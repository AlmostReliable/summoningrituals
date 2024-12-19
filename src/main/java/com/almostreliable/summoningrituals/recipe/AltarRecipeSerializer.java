package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.inventory.AltarInventory;
import com.almostreliable.summoningrituals.recipe.AltarRecipe.DAY_TIME;
import com.almostreliable.summoningrituals.recipe.AltarRecipe.WEATHER;
import com.almostreliable.summoningrituals.recipe.component.EntityOutput;
import com.almostreliable.summoningrituals.recipe.component.ItemOutput;
import com.almostreliable.summoningrituals.recipe.component.RecipeSacrifices;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

public class AltarRecipeSerializer implements RecipeSerializer<AltarRecipe> {

    public static final StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AltarRecipe decode(RegistryFriendlyByteBuf buffer) {
            var cata = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            var itemOuts = ItemOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
            var entityOuts = EntityOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
            var inputs = SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
            var sacrifices = ByteBufCodecs.optional(RecipeSacrifices.STREAM_CODEC).decode(buffer);
            var time = ByteBufCodecs.VAR_INT.decode(buffer);
            var blocks = ByteBufCodecs.optional(BlockPredicate.STREAM_CODEC).decode(buffer);
            var dayTime = DAY_TIME.STREAM_CODEC.decode(buffer);
            var weather = WEATHER.STREAM_CODEC.decode(buffer);
            return new AltarRecipe(cata, itemOuts, entityOuts, inputs, sacrifices, time, blocks, dayTime, weather);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, AltarRecipe value) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, value.catalyst());
            ItemOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, value.itemOutputs());
            EntityOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, value.entityOutputs());
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, value.inputs());
            ByteBufCodecs.optional(RecipeSacrifices.STREAM_CODEC).encode(buffer, value.sacrifices());
            ByteBufCodecs.VAR_INT.encode(buffer, value.recipeTime());
            ByteBufCodecs.optional(BlockPredicate.STREAM_CODEC).encode(buffer, value.blockBelow());
            DAY_TIME.STREAM_CODEC.encode(buffer, value.dayTime());
            WEATHER.STREAM_CODEC.encode(buffer, value.weather());
        }
    };

    private static final Codec<List<SizedIngredient>> INPUTS_CODEC = Codec.either(
        SizedIngredient.FLAT_CODEC,
        SizedIngredient.FLAT_CODEC.listOf()
    ).xmap(
        either -> either.map(List::of, List::copyOf),
        list -> list.size() == 1 ? Either.left(list.getFirst()) : Either.right(list)
    ).validate(inputs -> {
        if (inputs.size() >= AltarInventory.SIZE) {
            return DataResult.error(() -> "Too many inputs, max is " + AltarInventory.SIZE);
        }

        return DataResult.success(inputs);
    });
    @SuppressWarnings({"StaticNonFinalField", "NonConstantFieldWithUpperCaseName"}) public static int MAX_INPUTS;
    public static final MapCodec<AltarRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Ingredient.CODEC_NONEMPTY.fieldOf(
            "catalyst").forGetter(AltarRecipe::catalyst),
        ItemOutput.CODEC.listOf().optionalFieldOf("itemOutputs", List.of()).forGetter(AltarRecipe::itemOutputs),
        EntityOutput.CODEC.listOf().optionalFieldOf("entityOutputs", List.of()).forGetter(AltarRecipe::entityOutputs),
        INPUTS_CODEC.optionalFieldOf("inputs", List.of()).forGetter(AltarRecipe::inputs),
        RecipeSacrifices.CODEC.optionalFieldOf("sacrifices").forGetter(AltarRecipe::sacrifices),
        Codec.INT.optionalFieldOf("recipeTime", 100).forGetter(AltarRecipe::recipeTime),
        BlockPredicate.CODEC.optionalFieldOf("blockBelow").forGetter(AltarRecipe::blockBelow),
        DAY_TIME.CODEC.optionalFieldOf("dayTime", DAY_TIME.ANY).forGetter(AltarRecipe::dayTime),
        WEATHER.CODEC.optionalFieldOf("weather", WEATHER.ANY).forGetter(AltarRecipe::weather)
    ).apply(
        i,
        (ingredient, itemOutputs, entityOutputs, inputs, sacrifices, recipeTime, blockReference, dayTime, weather) -> {
            expandMaxInputs(inputs.size());
            return new AltarRecipe(
                ingredient,
                itemOutputs,
                entityOutputs,
                inputs,
                sacrifices,
                recipeTime,
                blockReference,
                dayTime,
                weather
            );
        }
    ));

    private static void expandMaxInputs(int amount) {
        if (MAX_INPUTS < amount) MAX_INPUTS = amount;
    }

    @Override
    public MapCodec<AltarRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}

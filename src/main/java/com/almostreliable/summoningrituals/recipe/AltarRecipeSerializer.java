package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.recipe.condition.CodecUtils;
import com.almostreliable.summoningrituals.recipe.condition.ConditionStreamCodecs;
import com.almostreliable.summoningrituals.recipe.input.EntityInput;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;
import com.almostreliable.summoningrituals.recipe.output.ItemOutput;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class AltarRecipeSerializer implements RecipeSerializer<AltarRecipe> {

    public static final MapCodec<AltarRecipe> CODEC = RecordCodecBuilder.<AltarRecipe> mapCodec(i -> i.group(
        Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(AltarRecipe::catalyst),
        ItemOutput.CODEC.listOf().optionalFieldOf("item_outputs", List.of()).forGetter(AltarRecipe::itemOutputs),
        EntityOutput.CODEC.listOf().optionalFieldOf("entity_outputs", List.of()).forGetter(AltarRecipe::entityOutputs),
        SizedIngredient.FLAT_CODEC.listOf().optionalFieldOf("item_inputs", List.of()).forGetter(AltarRecipe::itemInputs),
        EntityInput.CODEC.listOf().optionalFieldOf("entity_inputs", List.of()).forGetter(AltarRecipe::entityInputs),
        LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("start_conditions", List.of()).forGetter(AltarRecipe::startConditions),
        BlockPos.CODEC.optionalFieldOf("zone", AltarRecipe.DEFAULT_ZONE).forGetter(AltarRecipe::zone),
        Codec.INT.optionalFieldOf("ticks", AltarRecipe.DEFAULT_TICKS).forGetter(AltarRecipe::ticks)
    ).apply(i, AltarRecipe::new)).validate(AltarRecipeSerializer::validateRecipe);
    public static final StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> STREAM_CODEC = CodecUtils.composite(
        Ingredient.CONTENTS_STREAM_CODEC, AltarRecipe::catalyst,
        ItemOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), AltarRecipe::itemOutputs,
        EntityOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), AltarRecipe::entityOutputs,
        SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), AltarRecipe::itemInputs,
        EntityInput.STREAM_CODEC.apply(ByteBufCodecs.list()), AltarRecipe::entityInputs,
        ConditionStreamCodecs.LOOT_ITEM_CONDITION_STREAM_CODEC.apply(ByteBufCodecs.list()), AltarRecipe::startConditions,
        BlockPos.STREAM_CODEC, AltarRecipe::zone,
        ByteBufCodecs.VAR_INT, AltarRecipe::ticks,
        AltarRecipe::new
    );

    private static DataResult<AltarRecipe> validateRecipe(AltarRecipe recipe) {
        if (recipe.catalyst().isEmpty()) {
            return DataResult.error(() -> "catalyst is empty");
        }

        if (recipe.itemInputs().isEmpty() && recipe.entityInputs().isEmpty()) {
            return DataResult.error(() -> "no item or entity inputs");
        }

        if (recipe.itemInputs().size() > Config.COMMON.altarInventorySize) {
            return DataResult.error(() -> "too many inputs, max is " + Config.COMMON.altarInventorySize);
        }

        if (recipe.itemOutputs().isEmpty() && recipe.entityOutputs().isEmpty()) {
            return DataResult.error(() -> "no item or entity outputs");
        }

        for (var stack : recipe.catalyst().getItems()) {
            AltarRecipe.addCatalyst(stack.getItem());
        }
        for (var input : recipe.itemInputs()) {
            for (var stack : input.getItems()) {
                AltarRecipe.addInput(stack.getItem());
            }
        }

        return DataResult.success(recipe);
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

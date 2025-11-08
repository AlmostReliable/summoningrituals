package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.condition.ConditionStreamCodecs;
import com.almostreliable.summoningrituals.recipe.output.CommandOutput;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;
import com.almostreliable.summoningrituals.recipe.output.ItemOutput;
import com.almostreliable.summoningrituals.util.CodecUtils;

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
        Ingredient.CODEC_NONEMPTY.fieldOf(Constants.CATALYST).forGetter(AltarRecipe::catalyst),
        ItemOutput.CODEC.listOf().optionalFieldOf(Constants.ITEM_OUTPUTS, List.of()).forGetter(AltarRecipe::itemOutputs),
        EntityOutput.CODEC.listOf().optionalFieldOf(Constants.ENTITY_OUTPUTS, List.of()).forGetter(AltarRecipe::entityOutputs),
        CommandOutput.CODEC.optionalFieldOf(Constants.COMMANDS).forGetter(AltarRecipe::commands),
        SizedIngredient.FLAT_CODEC.listOf().optionalFieldOf(Constants.ITEM_INPUTS, List.of()).forGetter(AltarRecipe::itemInputs),
        EntityInfo.CODEC.listOf().optionalFieldOf(Constants.ENTITY_INPUTS, List.of()).forGetter(AltarRecipe::entityInputs),
        LootItemCondition.DIRECT_CODEC.listOf()
            .optionalFieldOf(Constants.CONDITIONS, List.of())
            .forGetter(AltarRecipe::startConditions),
        BlockPos.CODEC.optionalFieldOf(Constants.ZONE, AltarRecipe.DEFAULT_ZONE).forGetter(AltarRecipe::zone),
        Codec.INT.optionalFieldOf(Constants.TICKS, AltarRecipe.DEFAULT_TICKS).forGetter(AltarRecipe::ticks)
    ).apply(i, AltarRecipe::new)).validate(AltarRecipeSerializer::validateRecipe);
    public static final StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> STREAM_CODEC = CodecUtils.composite(
        Ingredient.CONTENTS_STREAM_CODEC, AltarRecipe::catalyst,
        ItemOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), AltarRecipe::itemOutputs,
        EntityOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), AltarRecipe::entityOutputs,
        ByteBufCodecs.optional(CommandOutput.STREAM_CODEC), AltarRecipe::commands,
        SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), AltarRecipe::itemInputs,
        EntityInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), AltarRecipe::entityInputs,
        ConditionStreamCodecs.CONDITION_STREAM_CODEC.apply(ByteBufCodecs.list()), AltarRecipe::startConditions,
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

        if (recipe.itemOutputs().isEmpty() && recipe.entityOutputs().isEmpty() && recipe.commands().isEmpty()) {
            return DataResult.error(() -> "no item, entity or command outputs");
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

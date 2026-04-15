package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.condition.ConditionStreamCodecs;
import com.almostreliable.summoningrituals.recipe.condition.pattern.BlockPatternCondition;
import com.almostreliable.summoningrituals.recipe.container.RecipeInputs;
import com.almostreliable.summoningrituals.recipe.container.RecipeOutputs;
import com.almostreliable.summoningrituals.util.CodecUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class AltarRecipeSerializer implements RecipeSerializer<AltarRecipe> {

    public static final MapCodec<AltarRecipe> CODEC = RecordCodecBuilder.<AltarRecipe> mapCodec(i -> i.group(
        Ingredient.CODEC_NONEMPTY.fieldOf(Constants.INITIATOR).forGetter(AltarRecipe::initiator),
        RecipeOutputs.MAP_CODEC.forGetter(AltarRecipe::outputs),
        RecipeInputs.MAP_CODEC.forGetter(AltarRecipe::inputs),
        LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf(Constants.CONDITIONS, List.of()).forGetter(AltarRecipe::conditions),
        BlockPatternCondition.CODEC.optionalFieldOf(Constants.BLOCK_PATTERN).forGetter(AltarRecipe::blockPattern),
        BlockPatternCondition.CODEC.optionalFieldOf(Constants.BLOCK_PATTERN_EXTENSION).forGetter(AltarRecipe::blockPatternExtension),
        BlockPos.CODEC.optionalFieldOf(Constants.ZONE, AltarRecipe.DEFAULT_ZONE).forGetter(AltarRecipe::zone),
        Codec.INT.optionalFieldOf(Constants.TICKS, AltarRecipe.DEFAULT_TICKS).forGetter(AltarRecipe::ticks)
    ).apply(i, AltarRecipe::new)).validate(AltarRecipeSerializer::validateRecipe);
    public static final StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> STREAM_CODEC = CodecUtils.composite(
        Ingredient.CONTENTS_STREAM_CODEC, AltarRecipe::initiator,
        RecipeOutputs.STREAM_CODEC, AltarRecipe::outputs,
        RecipeInputs.STREAM_CODEC, AltarRecipe::inputs,
        ConditionStreamCodecs.CONDITION_STREAM_CODEC.apply(ByteBufCodecs.list()), AltarRecipe::conditions,
        ByteBufCodecs.optional(BlockPatternCondition.STREAM_CODEC), AltarRecipe::blockPattern,
        ByteBufCodecs.optional(BlockPatternCondition.STREAM_CODEC), AltarRecipe::blockPatternExtension,
        BlockPos.STREAM_CODEC, AltarRecipe::zone,
        ByteBufCodecs.VAR_INT, AltarRecipe::ticks,
        AltarRecipe::new
    );

    private static DataResult<AltarRecipe> validateRecipe(AltarRecipe recipe) {
        if (recipe.initiator().isEmpty()) {
            return DataResult.error(() -> "initiator is empty");
        }

        var inputs = recipe.inputs();
        if (inputs.itemInputs().isEmpty() && inputs.entityInputs().isEmpty() && inputs.fakeEntityInputs().isEmpty()) {
            return DataResult.error(() -> "no item or entity inputs");
        }

        if (inputs.itemInputs().size() > Config.COMMON.inventorySize.get()) {
            return DataResult.error(() -> "too many inputs, max is " + Config.COMMON.inventorySize.get());
        }

        var outputs = recipe.outputs();
        if (outputs.itemOutputs().isEmpty() && outputs.entityOutputs().isEmpty() &&
            outputs.commandOutput().isEmpty() && outputs.displayOutputs().isEmpty()) {
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

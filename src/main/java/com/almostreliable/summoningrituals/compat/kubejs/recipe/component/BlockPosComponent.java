package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;

import net.minecraft.core.BlockPos;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public record BlockPosComponent(RecipeComponentType<?> type) implements RecipeComponent<BlockPos> {

    public static final RecipeComponentType<BlockPos> TYPE = RecipeComponentType.unit(
        SummoningRituals.getRL("block_pos"),
        BlockPosComponent::new
    );

    @Override
    public Codec<BlockPos> codec() {
        return BlockPos.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(BlockPos.class);
    }
}

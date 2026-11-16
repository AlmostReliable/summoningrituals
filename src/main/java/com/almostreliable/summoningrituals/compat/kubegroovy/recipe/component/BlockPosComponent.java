package com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;

import com.mojang.serialization.Codec;
import dev.uncandango.kubegroovy.recipe.component.RecipeComponent;
import dev.uncandango.kubegroovy.recipe.component.RecipeComponentType;
import dev.uncandango.kubegroovy.script.type.TypeInfo;

public record BlockPosComponent(ResourceKey<RecipeComponentType<?>> type) implements RecipeComponent<BlockPos> {

    public static final ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(SummoningRituals.getRL("block_pos"));
    public static final BlockPosComponent INSTANCE = new BlockPosComponent(TYPE);

    @Override
    public Codec<BlockPos> codec() {
        return BlockPos.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(BlockPos.class);
    }
}

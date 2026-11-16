package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

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

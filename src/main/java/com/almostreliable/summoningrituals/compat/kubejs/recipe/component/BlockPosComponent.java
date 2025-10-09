package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;

import net.minecraft.core.BlockPos;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.type.TypeInfo;

public class BlockPosComponent implements RecipeComponent<BlockPos> {

    public static final BlockPosComponent INSTANCE = new BlockPosComponent();

    @Override
    public Codec<BlockPos> codec() {
        return BlockPos.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(BlockPos.class);
    }

    @Override
    public String toString() {
        return SummoningRituals.getRL("block_pos").toString();
    }
}

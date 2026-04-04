package com.almostreliable.summoningrituals.recipe.condition;

import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.recipe.condition.custom.BlockPatternCheck;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public class BlockPatternCondition implements ConditionHandler<BlockPatternCheck> {

    public static final BlockPatternCondition INSTANCE = new BlockPatternCondition();

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BlockPatternCheck> getStreamCodec() {
        return BlockPatternCheck.STREAM_CODEC;
    }

    @Override
    public void getTooltip(List<Component> tooltip, BlockPatternCheck condition) {
        tooltip.add(Component.literal("- ").append(SummoningLang.BLOCK_PATTERN.get()));
    }
}

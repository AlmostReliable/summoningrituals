package com.almostreliable.summoningrituals.recipe.condition;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;

import java.util.List;

public class TimeCondition implements ConditionHandler<TimeCheck> {

    public static final TimeCondition INSTANCE = new TimeCondition();
    private static final StreamCodec<RegistryFriendlyByteBuf, TimeCheck> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG), TimeCheck::period,
        ConditionStreamCodecs.INT_RANGE_STREAM_CODEC, TimeCheck::value,
        TimeCheck::new
    );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, TimeCheck> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void getTooltip(List<Component> tooltip, TimeCheck condition) {
        tooltip.add(conditionComponent("TimeCheck"));
        tooltip.add(conditionValueComponent("min", condition.value().min.toString()));
        tooltip.add(conditionValueComponent("max", condition.value().max.toString()));
    }
}

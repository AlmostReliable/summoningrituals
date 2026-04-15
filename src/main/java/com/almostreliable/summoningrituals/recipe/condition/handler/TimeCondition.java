package com.almostreliable.summoningrituals.recipe.condition.handler;

import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.recipe.condition.ConditionStreamCodecs;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import org.jetbrains.annotations.Nullable;

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
        var range = condition.value();
        var min = range.min;
        var max = range.max;

        if (min instanceof ConstantValue(var minConstant) && max == null) {
            var minValue = (int) minConstant;
            var name = SummoningLang.MINIMUM.get().append(" ").append(SummoningLang.TIME.get());
            tooltip.add(conditionNameValueComponent(name, String.valueOf(minValue)));
            return;
        }

        if (max instanceof ConstantValue(var maxConstant) && min == null) {
            var maxValue = (int) maxConstant;
            var name = SummoningLang.MAXIMUM.get().append(" ").append(SummoningLang.TIME.get());
            tooltip.add(conditionNameValueComponent(name, String.valueOf(maxValue)));
            return;
        }

        if (!(min instanceof ConstantValue(var minConstant)) || !(max instanceof ConstantValue(var maxConstant))) {
            return;
        }

        var minValue = (int) minConstant;
        var maxValue = (int) maxConstant;

        var timeType = TimeType.of(minValue, maxValue);
        if (timeType != null) {
            tooltip.add(conditionNameValueComponent(SummoningLang.TIME.get(), SummoningLang.TIME_TYPES.get(timeType).get()));
            return;
        }

        tooltip.add(conditionNameComponent(SummoningLang.TIME.get()));
        tooltip.add(conditionNamedValueComponent(SummoningLang.MINIMUM.get(), minValue));
        tooltip.add(conditionNamedValueComponent(SummoningLang.MAXIMUM.get(), maxValue));
    }

    public enum TimeType {

        DAY(0, 12_000),
        NIGHT(12_000, 24_000),
        MORNING(0, 4_000),
        NOON(4_000, 8_000),
        AFTERNOON(8_000, 10_000),
        EVENING(10_000, 12_000),
        MIDNIGHT(17_000, 19_000);

        private final int min;
        private final int max;
        public final IntRange range;

        TimeType(int min, int max) {
            this.min = min;
            this.max = max;
            this.range = IntRange.range(min, max);
        }

        @Nullable
        private static TimeType of(int min, int max) {
            for (var type : values()) {
                if (type.min == min && type.max == max) return type;
            }
            return null;
        }
    }
}

package com.almostreliable.summoningrituals.recipe.condition.handler;

import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.data.SummoningLang.LangEntry;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class WeatherCondition implements ConditionHandler<WeatherCheck> {

    public static final WeatherCondition INSTANCE = new WeatherCondition();
    private static final StreamCodec<RegistryFriendlyByteBuf, WeatherCheck> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ByteBufCodecs.BOOL),
        WeatherCheck::isRaining,
        ByteBufCodecs.optional(ByteBufCodecs.BOOL),
        WeatherCheck::isThundering,
        WeatherCheck::new
    );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, WeatherCheck> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void getTooltip(List<Component> tooltip, WeatherCheck condition) {
        var description = getDescription(condition.isRaining(), condition.isThundering());
        if (description != null) {
            tooltip.add(conditionNameValueComponent(SummoningLang.WEATHER.get(), description.get()));
        }
    }

    @Nullable
    private static LangEntry getDescription(Optional<Boolean> rain, Optional<Boolean> thunder) {
        if (thunder.isPresent()) {
            if (thunder.get()) return SummoningLang.THUNDERING;

            if (rain.isPresent()) {
                if (!rain.get()) {
                    return SummoningLang.CLEAR;
                }
                return SummoningLang.RAINING;
            }

            return SummoningLang.NOT_THUNDERING;
        }

        return rain.map(r -> r ? SummoningLang.RAINING : SummoningLang.CLEAR).orElse(null);
    }

    public static final class Builder {

        private Optional<Boolean> isRaining = Optional.empty();
        private Optional<Boolean> isThundering = Optional.empty();

        public Builder setRaining(boolean isRaining) {
            this.isRaining = Optional.of(isRaining);
            return this;
        }

        public Builder setThundering(boolean isThundering) {
            this.isThundering = Optional.of(isThundering);
            return this;
        }

        public WeatherCheck build() {
            if (isRaining.isEmpty() && isThundering.isEmpty()) {
                throw new IllegalArgumentException("weather condition must have at least one of raining or thundering");
            }
            if (isRaining.isPresent() && isThundering.isPresent() && !isRaining.get() && isThundering.get()) {
                throw new IllegalArgumentException("weather condition cannot be thundering but not raining");
            }
            return new WeatherCheck(isRaining, isThundering);
        }
    }
}

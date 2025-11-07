package com.almostreliable.summoningrituals.recipe.condition;

import com.almostreliable.summoningrituals.data.SummoningLang.LangEntry;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class WeatherCondition implements ConditionHandler<WeatherCheck> {

    public static final WeatherCondition INSTANCE = new WeatherCondition();
    private static final StreamCodec<RegistryFriendlyByteBuf, WeatherCheck> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ByteBufCodecs.BOOL),
        WeatherCheck::isRaining,
        ByteBufCodecs.optional(ByteBufCodecs.BOOL),
        WeatherCheck::isThundering,
        WeatherCheck::new
    );

    private static final LangEntry WEATHER = LangEntry.condition("weather", "Weather");
    private static final LangEntry THUNDERING = LangEntry.condition("weather_thundering", "Thundering");
    private static final LangEntry NOT_THUNDERING = LangEntry.condition("weather_not_thundering", "Not Thundering");
    private static final LangEntry RAINING = LangEntry.condition("weather_raining", "Raining");
    private static final LangEntry CLEAR = LangEntry.condition("weather_clear", "Clear");

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, WeatherCheck> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void getTooltip(List<Component> tooltip, WeatherCheck condition) {
        var description = getDescription(condition.isRaining(), condition.isThundering());
        if (description != null) {
            tooltip.add(conditionNameValueComponent(WEATHER.get(), description.get()));
        }
    }

    @Nullable
    private static LangEntry getDescription(Optional<Boolean> rain, Optional<Boolean> thunder) {
        if (thunder.isPresent()) {
            if (thunder.get()) return THUNDERING;

            if (rain.isPresent()) {
                if (!rain.get()) {
                    return CLEAR;
                }
                return RAINING;
            }

            return NOT_THUNDERING;
        }

        return rain.map(r -> r ? RAINING : CLEAR).orElse(null);
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

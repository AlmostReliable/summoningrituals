package com.almostreliable.summoningrituals.recipe.condition;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

import java.util.List;

public class WeatherCondition implements ConditionHandler<WeatherCheck> {

    public static final WeatherCondition INSTANCE = new WeatherCondition();
    private static final StreamCodec<RegistryFriendlyByteBuf, WeatherCheck> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ByteBufCodecs.BOOL), WeatherCheck::isRaining,
        ByteBufCodecs.optional(ByteBufCodecs.BOOL), WeatherCheck::isThundering,
        WeatherCheck::new
    );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, WeatherCheck> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void getTooltip(List<Component> tooltip, WeatherCheck condition) {
        tooltip.add(conditionComponent("WeatherCheck"));
        if (condition.isRaining().isPresent()) {
            tooltip.add(conditionValueComponent("isRaining", condition.isRaining().get()));
        }
        if (condition.isThundering().isPresent()) {
            tooltip.add(conditionValueComponent("isThundering", condition.isThundering().get()));
        }
    }
}

package com.almostreliable.summoningrituals.compat.kubejs.builder;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class StartConditionsBuilder {

    private final List<LootItemCondition> conditions = new ArrayList<>();

    public StartConditionsBuilder weatherRaining() {
        conditions.add(WeatherCheck.weather().setRaining(true).build());
        return this;
    }

    public StartConditionsBuilder weatherThundering() {
        conditions.add(WeatherCheck.weather().setThundering(true).build());
        return this;
    }

    public StartConditionsBuilder weatherClear() {
        conditions.add(WeatherCheck.weather().setRaining(false).setThundering(false).build());
        return this;
    }

    public List<LootItemCondition> build() {
        Preconditions.checkArgument(
            conditions.stream().filter(WeatherCheck.class::isInstance).count() <= 1,
            "only one weather condition is allowed"
        );
        return conditions;
    }
}

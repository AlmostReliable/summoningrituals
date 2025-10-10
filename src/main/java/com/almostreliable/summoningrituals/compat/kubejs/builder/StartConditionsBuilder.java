package com.almostreliable.summoningrituals.compat.kubejs.builder;

import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class StartConditionsBuilder {

    private final List<LootItemCondition> conditions = new ArrayList<>();

    public StartConditionsBuilder weather(Function<WeatherCheck.Builder, WeatherCheck.Builder> weather) {
        var builder = WeatherCheck.weather();
        var weatherCheck = weather.apply(builder).build();
        conditions.add(weatherCheck);
        return this;
    }

    public StartConditionsBuilder location(Function<LocationPredicate.Builder, LocationPredicate.Builder> location) {
        var predicateBuilder = LocationPredicate.Builder.location();
        var locationCheck = LocationCheck.checkLocation(location.apply(predicateBuilder)).build();
        conditions.add(locationCheck);
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

package com.almostreliable.summoningrituals.compat.kubejs.builder;

import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public class StartConditionsBuilder {

    private final List<LootItemCondition> conditions = new ArrayList<>();

    public StartConditionsBuilder time(int min, int max) {
        conditions.add(new TimeCheck(Optional.empty(), IntRange.range(min, max)));
        return this;
    }

    public StartConditionsBuilder time(TimeTypes timeType) {
        conditions.add(new TimeCheck(Optional.empty(), timeType.range));
        return this;
    }

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

    public enum TimeTypes {
        DAY(0, 12_000),
        NIGHT(12_000, 24_000),
        MORNING(0, 4_000),
        NOON(4_000, 8_000),
        AFTERNOON(8_000, 10_000),
        EVENING(10_000, 12_000),
        MIDNIGHT(17_000, 19_000);

        private final IntRange range;

        TimeTypes(int min, int max) {
            this.range = IntRange.range(min, max);
        }
    }
}

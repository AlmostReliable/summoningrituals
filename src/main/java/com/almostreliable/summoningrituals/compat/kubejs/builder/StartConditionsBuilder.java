package com.almostreliable.summoningrituals.compat.kubejs.builder;

import com.almostreliable.summoningrituals.recipe.condition.TimeCondition;
import com.almostreliable.summoningrituals.recipe.condition.WeatherCondition;

import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;

import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.script.SourceLine;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO: implement more from the LocationPredicate (light, block below, water)
@SuppressWarnings("unused")
public final class StartConditionsBuilder {

    private final SourceLine sourceLine;
    private final List<LootItemCondition> conditions = new ArrayList<>();
    @Nullable
    private LocationPredicate.Builder locationPredicate;

    public StartConditionsBuilder(SourceLine sourceLine) {
        this.sourceLine = sourceLine;
    }

    public StartConditionsBuilder biomes(HolderSet<Biome> biomes) {
        getOrCreateLocationPredicate().setBiomes(biomes);
        return this;
    }

    public StartConditionsBuilder dimension(ResourceKey<Level> dimension) {
        getOrCreateLocationPredicate().setDimension(dimension);
        return this;
    }

    public StartConditionsBuilder minHeight(int min) {
        getOrCreateLocationPredicate().setY(MinMaxBounds.Doubles.atLeast(min));
        return this;
    }

    public StartConditionsBuilder maxHeight(int max) {
        getOrCreateLocationPredicate().setY(MinMaxBounds.Doubles.atMost(max));
        return this;
    }

    public StartConditionsBuilder height(int height) {
        getOrCreateLocationPredicate().setY(MinMaxBounds.Doubles.exactly(height));
        return this;
    }

    public StartConditionsBuilder height(int min, int max) {
        getOrCreateLocationPredicate().setY(MinMaxBounds.Doubles.between(min, max));
        return this;
    }

    public StartConditionsBuilder minTime(int min) {
        conditions.add(new TimeCheck(Optional.of(24_000L), IntRange.lowerBound(min)));
        return this;
    }

    public StartConditionsBuilder maxTime(int max) {
        conditions.add(new TimeCheck(Optional.of(24_000L), IntRange.upperBound(max)));
        return this;
    }

    public StartConditionsBuilder openSky(boolean openSky) {
        getOrCreateLocationPredicate().setCanSeeSky(openSky);
        return this;
    }

    public StartConditionsBuilder structures(HolderSet<Structure> structures) {
        getOrCreateLocationPredicate().setStructures(structures);
        return this;
    }

    public StartConditionsBuilder time(int min, int max) {
        conditions.add(new TimeCheck(Optional.of(24_000L), IntRange.range(min, max)));
        return this;
    }

    public StartConditionsBuilder time(TimeCondition.TimeType timeType) {
        conditions.add(new TimeCheck(Optional.of(24_000L), timeType.range));
        return this;
    }

    public StartConditionsBuilder weather(Function<WeatherCondition.Builder, WeatherCondition.Builder> weather) {
        try {
            var builder = new WeatherCondition.Builder();
            var weatherCheck = weather.apply(builder).build();
            conditions.add(weatherCheck);
        } catch (IllegalArgumentException e) {
            throwException(e.getMessage());
        }
        return this;
    }

    public List<LootItemCondition> build() {
        if (locationPredicate != null) {
            conditions.add(LocationCheck.checkLocation(locationPredicate).build());
        }

        var duplicates = conditions.stream()
            .collect(Collectors.groupingBy(LootItemCondition::getClass, Collectors.counting()))
            .entrySet().stream()
            .filter(e -> e.getValue() > 1)
            .map(Map.Entry::getKey)
            .map(Class::getSimpleName)
            .toList();

        Preconditions.checkArgument(
            duplicates.isEmpty(),
            "only one condition of each type allowed, duplicates found: %s"
        );
        return conditions;
    }

    private LocationPredicate.Builder getOrCreateLocationPredicate() {
        if (locationPredicate == null) {
            locationPredicate = LocationPredicate.Builder.location();
        }
        return locationPredicate;
    }

    private void throwException(String message) throws KubeRuntimeException {
        throw new KubeRuntimeException(message).source(sourceLine);
    }
}

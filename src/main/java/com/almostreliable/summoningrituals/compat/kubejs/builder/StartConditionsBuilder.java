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

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO: implement more from the LocationPredicate (light, block below, water)
@SuppressWarnings("unused")
public class StartConditionsBuilder {

    private final List<LootItemCondition> conditions = new ArrayList<>();
    @Nullable
    private HolderSet<Biome> biomes;
    @Nullable
    private ResourceKey<Level> dimension;
    @Nullable
    private MinMaxBounds.Doubles height;
    @Nullable
    private Boolean openSky;
    @Nullable
    private HolderSet<Structure> structures;

    public StartConditionsBuilder biomes(HolderSet<Biome> biomes) {
        Preconditions.checkArgument(this.biomes == null, "biomes have already been set");
        this.biomes = biomes;
        return this;
    }

    public StartConditionsBuilder dimension(ResourceKey<Level> dimension) {
        Preconditions.checkArgument(this.dimension == null, "dimension has already been set");
        this.dimension = dimension;
        return this;
    }

    public StartConditionsBuilder minHeight(int min) {
        Preconditions.checkArgument(height == null, "height has already been set");
        height = MinMaxBounds.Doubles.atLeast(min);
        return this;
    }

    public StartConditionsBuilder maxHeight(int max) {
        Preconditions.checkArgument(height == null, "height has already been set");
        height = MinMaxBounds.Doubles.atMost(max);
        return this;
    }

    public StartConditionsBuilder height(int height) {
        Preconditions.checkArgument(this.height == null, "height has already been set");
        this.height = MinMaxBounds.Doubles.exactly(height);
        return this;
    }

    public StartConditionsBuilder height(int min, int max) {
        Preconditions.checkArgument(height == null, "height has already been set");
        height = MinMaxBounds.Doubles.between(min, max);
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
        Preconditions.checkArgument(this.openSky == null, "openSky has already been set");
        this.openSky = openSky;
        return this;
    }

    public StartConditionsBuilder structures(HolderSet<Structure> structures) {
        Preconditions.checkArgument(this.structures == null, "structures have already been set");
        this.structures = structures;
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
        var builder = new WeatherCondition.Builder();
        var weatherCheck = weather.apply(builder).build();
        conditions.add(weatherCheck);
        return this;
    }

    public List<LootItemCondition> build() {
        var locationPredicate = LocationPredicate.Builder.location();
        if (biomes != null) locationPredicate.setBiomes(biomes);
        if (dimension != null) locationPredicate.setDimension(dimension);
        if (height != null) locationPredicate.setY(height);
        if (openSky != null) locationPredicate.setCanSeeSky(openSky);
        if (structures != null) locationPredicate.setStructures(structures);

        conditions.add(LocationCheck.checkLocation(locationPredicate).build());

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
}

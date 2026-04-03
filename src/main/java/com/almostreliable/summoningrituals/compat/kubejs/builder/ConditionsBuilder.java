package com.almostreliable.summoningrituals.compat.kubejs.builder;

import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.recipe.condition.TimeCondition;
import com.almostreliable.summoningrituals.recipe.condition.WeatherCondition;
import com.almostreliable.summoningrituals.recipe.condition.custom.MoonPhaseCheck;

import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.util.HideFromJS;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class ConditionsBuilder {

    private final List<LootItemCondition> conditions = new ArrayList<>();
    @Nullable
    private LocationPredicate.Builder locationPredicate;
    @Nullable
    private StatePropertiesPredicate.Builder blockStatePredicate;

    // region LocationCheck
    public ConditionsBuilder biomes(HolderSet<Biome> biomes) {
        getOrCreateLocationPredicate().setBiomes(biomes);
        return this;
    }

    public ConditionsBuilder dimension(ResourceKey<Level> dimension) {
        getOrCreateLocationPredicate().setDimension(dimension);
        return this;
    }

    public ConditionsBuilder minHeight(int min) {
        getOrCreateLocationPredicate().setY(MinMaxBounds.Doubles.atLeast(min));
        return this;
    }

    public ConditionsBuilder maxHeight(int max) {
        getOrCreateLocationPredicate().setY(MinMaxBounds.Doubles.atMost(max));
        return this;
    }

    public ConditionsBuilder height(int height) {
        getOrCreateLocationPredicate().setY(MinMaxBounds.Doubles.exactly(height));
        return this;
    }

    public ConditionsBuilder height(int min, int max) {
        getOrCreateLocationPredicate().setY(MinMaxBounds.Doubles.between(min, max));
        return this;
    }

    public ConditionsBuilder minLightLevel(int min) {
        getOrCreateLocationPredicate().setLight(LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.atLeast(min)));
        return this;
    }

    public ConditionsBuilder maxLightLevel(int max) {
        getOrCreateLocationPredicate().setLight(LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.atMost(max)));
        return this;
    }

    public ConditionsBuilder lightLevel(int lightLevel) {
        getOrCreateLocationPredicate().setLight(LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.exactly(lightLevel)));
        return this;
    }

    public ConditionsBuilder lightLevel(int min, int max) {
        getOrCreateLocationPredicate().setLight(LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.between(min, max)));
        return this;
    }

    public ConditionsBuilder setSmoked(boolean smoked) {
        getOrCreateLocationPredicate().setSmokey(smoked);
        return this;
    }

    public ConditionsBuilder setOpenSky(boolean openSky) {
        getOrCreateLocationPredicate().setCanSeeSky(openSky);
        return this;
    }

    public ConditionsBuilder structures(HolderSet<Structure> structures) {
        getOrCreateLocationPredicate().setStructures(structures);
        return this;
    }
    // endregion LocationCheck

    // region BlockStateCheck
    public ConditionsBuilder facing(Direction facing) {
        getOrCreateBlockStateCondition().hasProperty(BlockStateProperties.HORIZONTAL_FACING, facing);
        return this;
    }

    public ConditionsBuilder setWaterlogged(boolean waterlogged) {
        getOrCreateBlockStateCondition().hasProperty(BlockStateProperties.WATERLOGGED, waterlogged);
        return this;
    }
    // endregion BlockStateCheck

    // region Custom Conditions
    public ConditionsBuilder moonPhase(MoonPhaseCheck.MoonPhase phase) {
        conditions.add(new MoonPhaseCheck(phase));
        return this;
    }

    public ConditionsBuilder minTime(int min) {
        conditions.add(new TimeCheck(Optional.of(24_000L), IntRange.lowerBound(min)));
        return this;
    }

    public ConditionsBuilder maxTime(int max) {
        conditions.add(new TimeCheck(Optional.of(24_000L), IntRange.upperBound(max)));
        return this;
    }

    public ConditionsBuilder time(int min, int max) {
        conditions.add(new TimeCheck(Optional.of(24_000L), IntRange.range(min, max)));
        return this;
    }

    public ConditionsBuilder time(TimeCondition.TimeType timeType) {
        conditions.add(new TimeCheck(Optional.of(24_000L), timeType.range));
        return this;
    }

    public ConditionsBuilder weather(Context ctx, Function<WeatherCondition.Builder, WeatherCondition.Builder> weather) {
        try {
            var builder = new WeatherCondition.Builder();
            var weatherCheck = weather.apply(builder).build();
            conditions.add(weatherCheck);
        } catch (IllegalArgumentException e) {
            throwException(ctx, e.getMessage());
        }
        return this;
    }
    // endregion Custom Conditions

    @HideFromJS
    public List<LootItemCondition> build(Context ctx) {
        if (locationPredicate != null) {
            conditions.add(LocationCheck.checkLocation(locationPredicate).build());
        }
        if (blockStatePredicate != null) {
            var altarStatePredicate = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Registration.ALTAR_BLOCK.get())
                .setProperties(blockStatePredicate);
            var indesAltarStatePredicate = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Registration.INDESTRUCTIBLE_ALTAR_BLOCK.get())
                .setProperties(blockStatePredicate);

            // TODO: replace with custom loot condition that supports a tag instead of AnyOf
            conditions.add(AnyOfCondition.anyOf(altarStatePredicate, indesAltarStatePredicate).build());
        }

        var duplicates = conditions.stream()
            .collect(Collectors.groupingBy(LootItemCondition::getClass, Collectors.counting()))
            .entrySet().stream()
            .filter(e -> e.getValue() > 1)
            .map(Map.Entry::getKey)
            .map(Class::getSimpleName)
            .toList();

        if (!duplicates.isEmpty()) {
            throwException(ctx, "only one condition of each type allowed, duplicates found: " + duplicates);
        }

        return conditions;
    }

    private LocationPredicate.Builder getOrCreateLocationPredicate() {
        if (locationPredicate == null) {
            locationPredicate = LocationPredicate.Builder.location();
        }
        return locationPredicate;
    }

    private StatePropertiesPredicate.Builder getOrCreateBlockStateCondition() {
        if (blockStatePredicate == null) {
            blockStatePredicate = StatePropertiesPredicate.Builder.properties();
        }
        return blockStatePredicate;
    }

    private void throwException(Context ctx, String message) throws KubeRuntimeException {
        throw new KubeRuntimeException(message).source(SourceLine.of(ctx));
    }
}

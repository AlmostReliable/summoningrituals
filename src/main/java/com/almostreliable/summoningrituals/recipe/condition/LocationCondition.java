package com.almostreliable.summoningrituals.recipe.condition;

import com.almostreliable.summoningrituals.data.SummoningLang.LangEntry;
import com.almostreliable.summoningrituals.util.RawHolderSet;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.neoforged.fml.ModList;

import com.google.common.base.CaseFormat;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

public class LocationCondition implements ConditionHandler<LocationCheck> {

    public static final LocationCondition INSTANCE = new LocationCondition();
    private static final StreamCodec<RegistryFriendlyByteBuf, LocationCheck> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ConditionStreamCodecs.LOCATION_PREDICATE_STREAM_CODEC), LocationCheck::predicate,
        BlockPos.STREAM_CODEC, LocationCheck::offset,
        LocationCheck::new
    );

    private static final LangEntry BIOMES = LangEntry.condition("biomes", "Biomes");
    private static final LangEntry DIMENSION = LangEntry.condition("dimension", "Dimension");
    private static final LangEntry HEIGHT = LangEntry.condition("height", "Height");
    private static final LangEntry LIGHT_LEVEL = LangEntry.condition("light_level", "Light Level");
    private static final LangEntry SMOKED = LangEntry.condition("smoked", "Smoked");
    private static final LangEntry OPEN_SKY = LangEntry.condition("open_sky", "Open Sky");
    private static final LangEntry STRUCTURES = LangEntry.condition("structures", "Structures");

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, LocationCheck> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void getTooltip(List<Component> tooltip, LocationCheck condition) {
        var opt = condition.predicate();
        if (opt.isEmpty()) return;
        var predicate = opt.get();

        if (predicate.biomes().isPresent()) {
            tooltip.add(conditionNameComponent(BIOMES.get()));
            addHolderSetTooltip(tooltip, predicate.biomes().get());
        }
        if (predicate.dimension().isPresent()) {
            var dimension = predicate.dimension().get();
            tooltip.add(conditionNameValueComponent(DIMENSION.get(), getReadableId(dimension.location())));
        }
        if (predicate.position().isPresent()) {
            var position = predicate.position().get();
            var yPos = position.y();
            addHeightTooltip(tooltip, yPos);
        }
        if (predicate.light().isPresent()) {
            var lightLevel = predicate.light().get();
            addLightLevelTooltip(tooltip, lightLevel.composite());
        }
        if (predicate.smokey().isPresent()) {
            var smoked = predicate.smokey().get();
            var value = (smoked ? YES : NO).get();
            tooltip.add(conditionNameValueComponent(SMOKED.get(), value));
        }
        if (predicate.canSeeSky().isPresent()) {
            var openSky = predicate.canSeeSky().get();
            var value = (openSky ? YES : NO).get();
            tooltip.add(conditionNameValueComponent(OPEN_SKY.get(), value));
        }
        if (predicate.structures().isPresent()) {
            tooltip.add(conditionNameComponent(STRUCTURES.get()));
            addHolderSetTooltip(tooltip, predicate.structures().get());
        }
    }

    private void addHeightTooltip(List<Component> tooltip, MinMaxBounds.Doubles yPos) {
        addMinMaxBoundsTooltip(
            tooltip,
            yPos.min().map(Double::intValue),
            yPos.max().map(Double::intValue),
            HEIGHT.get(),
            Objects::equals
        );
    }

    private void addLightLevelTooltip(List<Component> tooltip, MinMaxBounds.Ints lightLevel) {
        addMinMaxBoundsTooltip(
            tooltip,
            lightLevel.min(),
            lightLevel.max(),
            LIGHT_LEVEL.get(),
            Integer::equals
        );
    }

    private void addHolderSetTooltip(List<Component> tooltip, HolderSet<?> holders) {
        if (holders instanceof RawHolderSet<?> rawHolderSet) {
            if (rawHolderSet.tag().isPresent()) {
                var tag = rawHolderSet.tag().get();
                var tagId = tag.location().toString();
                tooltip.add(conditionValueComponent("#" + tagId));
                return;
            }

            if (rawHolderSet.ids().isEmpty()) return;
            var ids = rawHolderSet.ids().get();
            for (var id : ids) {
                var readableId = getReadableId(id);
                tooltip.add(conditionValueComponent(readableId));
            }
            return;
        }

        var holderValue = holders.unwrap();
        holderValue.ifLeft(tag -> {
            var tagId = tag.location().toString();
            tooltip.add(conditionValueComponent("#" + tagId));
        });
        holderValue.ifRight(holderList -> {
            for (var holder : holderList) {
                var resourceKey = holder.unwrapKey();
                if (resourceKey.isEmpty()) {
                    tooltip.add(conditionValueComponent("unknown"));
                    continue;
                }

                var readableId = getReadableId(resourceKey.get().location());
                tooltip.add(conditionValueComponent(readableId));
            }
        });
    }

    private static String getReadableId(ResourceLocation id) {
        var namespace = id.getNamespace();
        var path = id.getPath();

        var readableName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, path);
        if (!namespace.equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            var modContainer = ModList.get().getModContainerById(namespace);
            if (modContainer.isEmpty()) {
                return readableName + " (" + namespace + ")";
            }

            var modName = modContainer.get().getModInfo().getDisplayName();
            return readableName + " (" + modName + ")";
        }

        return readableName;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private <T> void addMinMaxBoundsTooltip(
        List<Component> tooltip,
        Optional<T> min,
        Optional<T> max,
        Component name,
        BiPredicate<T, T> equalityCheck
    ) {
        if (min.isPresent() && max.isPresent()) {
            var minValue = min.get();
            var maxValue = max.get();

            if (equalityCheck.test(minValue, maxValue)) {
                tooltip.add(conditionNameValueComponent(name, String.valueOf(minValue)));
                return;
            }

            tooltip.add(conditionNameComponent(name));
            tooltip.add(conditionNamedValueComponent(MINIMUM.get(), String.valueOf(minValue)));
            tooltip.add(conditionNamedValueComponent(MAXIMUM.get(), String.valueOf(maxValue)));

            return;
        }

        if (min.isPresent()) {
            var minValue = min.get();
            var minName = MINIMUM.get().append(" ").append(name);
            tooltip.add(conditionNameValueComponent(minName, String.valueOf(minValue)));
            return;
        }

        if (max.isPresent()) {
            var maxValue = max.get();
            var maxName = MAXIMUM.get().append(" ").append(name);
            tooltip.add(conditionNameValueComponent(maxName, String.valueOf(maxValue)));
        }
    }
}

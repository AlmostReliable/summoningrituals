package com.almostreliable.summoningrituals.recipe.condition.handler;

import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.recipe.condition.ConditionStreamCodecs;
import com.almostreliable.summoningrituals.util.RawHolderSet;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
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
            tooltip.add(conditionNameComponent(SummoningLang.BIOMES.get()));
            addHolderSetTooltip(tooltip, predicate.biomes().get());
        }
        if (predicate.dimension().isPresent()) {
            var dimension = predicate.dimension().get();
            tooltip.add(conditionNameValueComponent(SummoningLang.DIMENSION.get(), getReadableId(dimension.location())));
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
            var value = (smoked ? SummoningLang.YES : SummoningLang.NO).get();
            tooltip.add(conditionNameValueComponent(SummoningLang.SMOKED.get(), value));
        }
        if (predicate.canSeeSky().isPresent()) {
            var openSky = predicate.canSeeSky().get();
            var value = (openSky ? SummoningLang.YES : SummoningLang.NO).get();
            tooltip.add(conditionNameValueComponent(SummoningLang.OPEN_SKY.get(), value));
        }
        if (predicate.structures().isPresent()) {
            tooltip.add(conditionNameComponent(SummoningLang.STRUCTURES.get()));
            addHolderSetTooltip(tooltip, predicate.structures().get());
        }
    }

    private void addHeightTooltip(List<Component> tooltip, MinMaxBounds.Doubles yPos) {
        addMinMaxBoundsTooltip(
            tooltip,
            yPos.min().map(Double::intValue),
            yPos.max().map(Double::intValue),
            SummoningLang.HEIGHT.get(),
            Objects::equals
        );
    }

    private void addLightLevelTooltip(List<Component> tooltip, MinMaxBounds.Ints lightLevel) {
        addMinMaxBoundsTooltip(
            tooltip,
            lightLevel.min(),
            lightLevel.max(),
            SummoningLang.LIGHT_LEVEL.get(),
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
                var readableId = appendModId(getReadableId(id), id);
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
                var resourceKeyOpt = holder.unwrapKey();
                if (resourceKeyOpt.isEmpty()) {
                    tooltip.add(conditionValueComponent("unknown"));
                    continue;
                }

                var resourceKey = resourceKeyOpt.get();
                var id = resourceKey.location();
                var readableId = getReadableId(id);

                var registryKey = resourceKey.registryKey();
                if (Registries.BIOME.equals(registryKey)) {
                    var componentKey = id.toLanguageKey("biome");
                    var component = Component.translatableWithFallback(componentKey, readableId);
                    readableId = component.getString();
                }

                readableId = appendModId(readableId, id);
                tooltip.add(conditionValueComponent(readableId));
            }
        });
    }

    private static String getReadableId(ResourceLocation id) {
        var namespace = id.getNamespace();
        var path = id.getPath();
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, path);
    }

    private static String appendModId(String text, ResourceLocation id) {
        var namespace = id.getNamespace();
        if (namespace.equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            return text;
        }

        var modContainer = ModList.get().getModContainerById(namespace);
        if (modContainer.isEmpty()) {
            return text + " (" + namespace + ")";
        }

        var modName = modContainer.get().getModInfo().getDisplayName();
        return text + " (" + modName + ")";
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
            tooltip.add(conditionNamedValueComponent(SummoningLang.MINIMUM.get(), String.valueOf(minValue)));
            tooltip.add(conditionNamedValueComponent(SummoningLang.MAXIMUM.get(), String.valueOf(maxValue)));

            return;
        }

        if (min.isPresent()) {
            var minValue = min.get();
            var minName = SummoningLang.MINIMUM.get().append(" ").append(name);
            tooltip.add(conditionNameValueComponent(minName, String.valueOf(minValue)));
            return;
        }

        if (max.isPresent()) {
            var maxValue = max.get();
            var maxName = SummoningLang.MAXIMUM.get().append(" ").append(name);
            tooltip.add(conditionNameValueComponent(maxName, String.valueOf(maxValue)));
        }
    }
}

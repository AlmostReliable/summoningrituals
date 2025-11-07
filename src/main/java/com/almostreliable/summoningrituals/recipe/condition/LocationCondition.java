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

public class LocationCondition implements ConditionHandler<LocationCheck> {

    public static final LocationCondition INSTANCE = new LocationCondition();

    public static final StreamCodec<RegistryFriendlyByteBuf, LocationCheck> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ConditionStreamCodecs.LOCATION_PREDICATE_STREAM_CODEC), LocationCheck::predicate,
        BlockPos.STREAM_CODEC, LocationCheck::offset,
        LocationCheck::new
    );

    private static final LangEntry BIOMES = LangEntry.condition("biomes", "Biomes");
    private static final LangEntry DIMENSION = LangEntry.condition("dimension", "Dimension");
    private static final LangEntry HEIGHT = LangEntry.condition("height", "Height");
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
            getHolderSetTooltip(tooltip, predicate.biomes().get());
        }
        if (predicate.dimension().isPresent()) {
            var dimension = predicate.dimension().get();
            tooltip.add(conditionNameValueComponent(DIMENSION.get(), getReadableId(dimension.location())));
        }
        if (predicate.position().isPresent()) {
            var position = predicate.position().get();
            var yPos = position.y();
            getHeightTooltip(tooltip, yPos);
        }
        if (predicate.canSeeSky().isPresent()) {
            var openSky = predicate.canSeeSky().get();
            var value = (openSky ? YES : NO).get();
            tooltip.add(conditionNameValueComponent(OPEN_SKY.get(), value));
        }
        if (predicate.structures().isPresent()) {
            tooltip.add(conditionNameComponent(STRUCTURES.get()));
            getHolderSetTooltip(tooltip, predicate.structures().get());
        }
    }

    private void getHeightTooltip(List<Component> tooltip, MinMaxBounds.Doubles yPos) {
        var min = yPos.min();
        var max = yPos.max();

        if (min.isPresent() && max.isPresent()) {
            var minValue = min.get().intValue();
            var maxValue = max.get().intValue();

            if (minValue == maxValue) {
                tooltip.add(conditionNameValueComponent(HEIGHT.get(), String.valueOf(minValue)));
                return;
            }

            tooltip.add(conditionNameComponent(HEIGHT.get()));
            tooltip.add(conditionNamedValueComponent(MINIMUM.get(), String.valueOf(minValue)));
            tooltip.add(conditionNamedValueComponent(MAXIMUM.get(), String.valueOf(maxValue)));

            return;
        }

        if (min.isPresent()) {
            var minValue = min.get().intValue();
            var name = MINIMUM.get().append(" ").append(HEIGHT.get());
            tooltip.add(conditionNameValueComponent(name, String.valueOf(minValue)));
            return;
        }

        if (max.isPresent()) {
            var maxValue = max.get().intValue();
            var name = MAXIMUM.get().append(" ").append(HEIGHT.get());
            tooltip.add(conditionNameValueComponent(name, String.valueOf(maxValue)));
        }
    }

    private void getHolderSetTooltip(List<Component> tooltip, HolderSet<?> holders) {
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
}

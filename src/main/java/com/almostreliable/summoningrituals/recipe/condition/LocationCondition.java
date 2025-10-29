package com.almostreliable.summoningrituals.recipe.condition;

import com.almostreliable.summoningrituals.util.CodecUtils;
import com.almostreliable.summoningrituals.util.RawHolderSetStreamCodec;

import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocationCondition implements ConditionHandler<LocationCheck> {

    public static final LocationCondition INSTANCE = new LocationCondition();
    private static final StreamCodec<RegistryFriendlyByteBuf, LocationPredicate> LOCATION_PREDICATE_STREAM_CODEC = CodecUtils.composite(
        CodecUtils.emptyOptionalStreamCodec(), $ -> Optional.empty(),
        ByteBufCodecs.optional(ByteBufCodecs.holderSet(Registries.BIOME)), LocationPredicate::biomes,
        ByteBufCodecs.optional(new RawHolderSetStreamCodec<>(Registries.STRUCTURE)), LocationPredicate::structures,
        ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.DIMENSION)), LocationPredicate::dimension,
        CodecUtils.emptyOptionalStreamCodec(), $ -> Optional.empty(),
        CodecUtils.emptyOptionalStreamCodec(), $ -> Optional.empty(),
        CodecUtils.emptyOptionalStreamCodec(), $ -> Optional.empty(),
        CodecUtils.emptyOptionalStreamCodec(), $ -> Optional.empty(),
        ByteBufCodecs.optional(ByteBufCodecs.BOOL), LocationPredicate::canSeeSky,
        LocationPredicate::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, LocationCheck> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(LOCATION_PREDICATE_STREAM_CODEC), LocationCheck::predicate,
        BlockPos.STREAM_CODEC, LocationCheck::offset,
        LocationCheck::new
    );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, LocationCheck> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void getTooltip(List<Component> tooltip, LocationCheck condition) {
        tooltip.add(conditionComponent("LocationCheck"));
        //noinspection OptionalGetWithoutIsPresent
        var locationPredicate = condition.predicate().get();
        if (locationPredicate.biomes().isPresent()) {
            tooltip.add(conditionValueComponent("biomes", readableHolderSet(locationPredicate.biomes().get())));
        }
        if (locationPredicate.structures().isPresent()) {
            tooltip.add(conditionValueComponent("structures", locationPredicate.structures().get()));
        }
        if (locationPredicate.dimension().isPresent()) {
            tooltip.add(conditionValueComponent("dimension", locationPredicate.dimension().get().location()));
        }
        if (locationPredicate.canSeeSky().isPresent()) {
            tooltip.add(conditionValueComponent("canSeeSky", locationPredicate.canSeeSky().get()));
        }
    }

    private static String readableHolderSet(HolderSet<?> holders) {
        var holderNames = new ArrayList<String>();
        for (var holder : holders) {
            holderNames.add(holder.getRegisteredName());
        }
        return String.join(", ", holderNames);
    }
}

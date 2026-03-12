package com.almostreliable.summoningrituals.recipe.condition;

import com.almostreliable.summoningrituals.util.CodecUtils;
import com.almostreliable.summoningrituals.util.RawHolderSetStreamCodec;

import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import java.util.Optional;

public final class ConditionStreamCodecs {

    public static final StreamCodec<RegistryFriendlyByteBuf, NumberProvider> NUMBER_PROVIDER_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(
        NumberProviders.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, IntRange> INT_RANGE_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(
        IntRange.CODEC);
    public static final StreamCodec<FriendlyByteBuf, MinMaxBounds.Ints> INTS_BOUNDS_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ByteBufCodecs.INT), MinMaxBounds.Ints::min,
        ByteBufCodecs.optional(ByteBufCodecs.INT), MinMaxBounds.Ints::max,
        ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG), MinMaxBounds.Ints::minSq,
        ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG), MinMaxBounds.Ints::maxSq,
        MinMaxBounds.Ints::new
    );
    public static final StreamCodec<FriendlyByteBuf, MinMaxBounds.Doubles> DOUBLES_BOUNDS_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ByteBufCodecs.DOUBLE), MinMaxBounds.Doubles::min,
        ByteBufCodecs.optional(ByteBufCodecs.DOUBLE), MinMaxBounds.Doubles::max,
        ByteBufCodecs.optional(ByteBufCodecs.DOUBLE), MinMaxBounds.Doubles::minSq,
        ByteBufCodecs.optional(ByteBufCodecs.DOUBLE), MinMaxBounds.Doubles::maxSq,
        MinMaxBounds.Doubles::new
    );
    public static final StreamCodec<FriendlyByteBuf, LocationPredicate.PositionPredicate> POSITION_PREDICATE_STREAM_CODEC = StreamCodec.composite(
        DOUBLES_BOUNDS_STREAM_CODEC, LocationPredicate.PositionPredicate::x,
        DOUBLES_BOUNDS_STREAM_CODEC, LocationPredicate.PositionPredicate::y,
        DOUBLES_BOUNDS_STREAM_CODEC, LocationPredicate.PositionPredicate::z,
        LocationPredicate.PositionPredicate::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, LightPredicate> LIGHT_PREDICATE_STREAM_CODEC = StreamCodec.composite(
        INTS_BOUNDS_STREAM_CODEC, LightPredicate::composite,
        LightPredicate::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, LocationPredicate> LOCATION_PREDICATE_STREAM_CODEC = CodecUtils.composite(
        ByteBufCodecs.optional(POSITION_PREDICATE_STREAM_CODEC), LocationPredicate::position,
        ByteBufCodecs.optional(ByteBufCodecs.holderSet(Registries.BIOME)), LocationPredicate::biomes,
        ByteBufCodecs.optional(new RawHolderSetStreamCodec<>(Registries.STRUCTURE)), LocationPredicate::structures,
        ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.DIMENSION)), LocationPredicate::dimension,
        CodecUtils.emptyOptionalStreamCodec(), $ -> Optional.empty(),
        ByteBufCodecs.optional(LIGHT_PREDICATE_STREAM_CODEC), LocationPredicate::light,
        CodecUtils.emptyOptionalStreamCodec(), $ -> Optional.empty(),
        CodecUtils.emptyOptionalStreamCodec(), $ -> Optional.empty(),
        ByteBufCodecs.optional(ByteBufCodecs.BOOL), LocationPredicate::canSeeSky,
        LocationPredicate::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, LootItemCondition> CONDITION_STREAM_CODEC = new StreamCodec<>() {

        @Override
        public LootItemCondition decode(RegistryFriendlyByteBuf buffer) {
            var type = BuiltInRegistries.LOOT_CONDITION_TYPE.byIdOrThrow(buffer.readByte());
            var conditionHandler = ConditionRegistry.getOrThrow(type);
            var streamCodec = conditionHandler.getStreamCodec();
            return streamCodec.decode(buffer);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, LootItemCondition value) {
            var id = BuiltInRegistries.LOOT_CONDITION_TYPE.getId(value.getType());
            buffer.writeByte(id);
            var conditionHandler = ConditionRegistry.getOrThrow(value.getType());
            var streamCodec = conditionHandler.getStreamCodec();
            streamCodec.encode(buffer, value);
        }
    };

    private ConditionStreamCodecs() {}
}

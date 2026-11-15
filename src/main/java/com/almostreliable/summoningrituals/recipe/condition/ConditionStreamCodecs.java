package com.almostreliable.summoningrituals.recipe.condition;

import com.almostreliable.summoningrituals.util.CodecUtils;
import com.almostreliable.summoningrituals.util.RawHolderSetStreamCodec;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.LightPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
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

import io.netty.buffer.ByteBuf;

import java.util.Optional;

public final class ConditionStreamCodecs {

    public static final StreamCodec<RegistryFriendlyByteBuf, NumberProvider> NUMBER_PROVIDER_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(
        NumberProviders.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, IntRange> INT_RANGE_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(
        IntRange.CODEC);
    public static final StreamCodec<ByteBuf, MinMaxBounds.Ints> INTS_BOUNDS_STREAM_CODEC = MinMaxBounds.Ints.STREAM_CODEC;
    public static final StreamCodec<ByteBuf, MinMaxBounds.Doubles> DOUBLES_BOUNDS_STREAM_CODEC = MinMaxBounds.Doubles.STREAM_CODEC;
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
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPredicate> BLOCK_PREDICATE_STREAM_CODEC = BlockPredicate.STREAM_CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, LocationPredicate> LOCATION_PREDICATE_STREAM_CODEC = CodecUtils.composite(
        ByteBufCodecs.optional(POSITION_PREDICATE_STREAM_CODEC), LocationPredicate::position,
        ByteBufCodecs.optional(ByteBufCodecs.holderSet(Registries.BIOME)), LocationPredicate::biomes,
        ByteBufCodecs.optional(new RawHolderSetStreamCodec<>(Registries.STRUCTURE)), LocationPredicate::structures,
        ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.DIMENSION)), LocationPredicate::dimension,
        ByteBufCodecs.optional(ByteBufCodecs.BOOL), LocationPredicate::smokey,
        ByteBufCodecs.optional(LIGHT_PREDICATE_STREAM_CODEC), LocationPredicate::light,
        ByteBufCodecs.optional(BLOCK_PREDICATE_STREAM_CODEC), LocationPredicate::block,
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
            var id = BuiltInRegistries.LOOT_CONDITION_TYPE.getId(value.codec());
            buffer.writeByte(id);
            var conditionHandler = ConditionRegistry.getOrThrow(value.codec());
            var streamCodec = conditionHandler.getStreamCodec();
            streamCodec.encode(buffer, value);
        }
    };

    private ConditionStreamCodecs() {}
}

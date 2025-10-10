package com.almostreliable.summoningrituals.recipe.condition;

import com.almostreliable.summoningrituals.network.RawHolderSetStreamCodec;

import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Optional;

public final class ConditionStreamCodecs {

    public static final StreamCodec<FriendlyByteBuf, NumberProvider> NUMBER_PROVIDER_STREAM_CODEC = new StreamCodec<>() {

        private static final byte CONSTANT_ID = 0;
        private static final byte UNIFORM_ID = 1;
        private static final byte BINOMIAL_ID = 2;

        @Override
        public NumberProvider decode(FriendlyByteBuf buffer) {
            var id = buffer.readByte();
            return switch (id) {
                case CONSTANT_ID -> new ConstantValue(buffer.readFloat());
                case UNIFORM_ID -> new UniformGenerator(decode(buffer), decode(buffer));
                case BINOMIAL_ID -> new BinomialDistributionGenerator(decode(buffer), decode(buffer));
                default -> throw new IllegalArgumentException("Unknown NumberProvider type");
            };
        }

        @Override
        public void encode(FriendlyByteBuf buffer, NumberProvider value) {
            switch (value) {
                case ConstantValue constant -> {
                    buffer.writeByte(CONSTANT_ID);
                    buffer.writeFloat(constant.value());
                }
                case UniformGenerator generator -> {
                    buffer.writeByte(UNIFORM_ID);
                    encode(buffer, generator.min());
                    encode(buffer, generator.max());
                }
                case BinomialDistributionGenerator binomial -> {
                    buffer.writeByte(BINOMIAL_ID);
                    encode(buffer, binomial.n());
                    encode(buffer, binomial.p());
                }
                default -> throw new IllegalArgumentException("Unknown NumberProvider type");
            }
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, LocationPredicate> LOCATION_PREDICATE_STREAM_CODEC = CodecUtils.composite(
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

    public static final StreamCodec<FriendlyByteBuf, IntRange> INT_RANGE_STREAM_CODEC = StreamCodec.composite(
        NUMBER_PROVIDER_STREAM_CODEC, intRange -> intRange.min,
        NUMBER_PROVIDER_STREAM_CODEC, intRange -> intRange.max,
        IntRange::new
    );

    public static final StreamCodec<FriendlyByteBuf, TimeCheck> TIME_CHECK_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG), TimeCheck::period,
        INT_RANGE_STREAM_CODEC, TimeCheck::value,
        TimeCheck::new
    );

    public static final StreamCodec<FriendlyByteBuf, WeatherCheck> WEATHER_CHECK_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ByteBufCodecs.BOOL), WeatherCheck::isRaining,
        ByteBufCodecs.optional(ByteBufCodecs.BOOL), WeatherCheck::isThundering,
        WeatherCheck::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, LocationCheck> LOCATION_CHECK_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(LOCATION_PREDICATE_STREAM_CODEC), LocationCheck::predicate,
        BlockPos.STREAM_CODEC, LocationCheck::offset,
        LocationCheck::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, LootItemCondition> LOOT_ITEM_CONDITION_STREAM_CODEC = new StreamCodec<>() {

        private static final byte TIME_CHECK_ID = 0;
        private static final byte WEATHER_CHECK_ID = 1;
        private static final byte LOCATION_CHECK_ID = 2;

        @Override
        public LootItemCondition decode(RegistryFriendlyByteBuf buffer) {
            var id = buffer.readByte();
            return switch (id) {
                case TIME_CHECK_ID -> TIME_CHECK_STREAM_CODEC.decode(buffer);
                case WEATHER_CHECK_ID -> WEATHER_CHECK_STREAM_CODEC.decode(buffer);
                case LOCATION_CHECK_ID -> LOCATION_CHECK_STREAM_CODEC.decode(buffer);
                default -> throw new IllegalArgumentException("Unknown LootItemCondition type");
            };
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, LootItemCondition value) {
            switch (value) {
                case TimeCheck timeCheck -> {
                    buffer.writeByte(TIME_CHECK_ID);
                    TIME_CHECK_STREAM_CODEC.encode(buffer, timeCheck);
                }
                case WeatherCheck weatherCheck -> {
                    buffer.writeByte(WEATHER_CHECK_ID);
                    WEATHER_CHECK_STREAM_CODEC.encode(buffer, weatherCheck);
                }
                case LocationCheck locationCheck -> {
                    buffer.writeByte(LOCATION_CHECK_ID);
                    LOCATION_CHECK_STREAM_CODEC.encode(buffer, locationCheck);
                }
                default -> throw new IllegalArgumentException("Unknown LootItemCondition type");
            }
        }
    };

    private ConditionStreamCodecs() {}
}

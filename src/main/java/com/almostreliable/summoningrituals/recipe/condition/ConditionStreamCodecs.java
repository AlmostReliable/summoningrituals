package com.almostreliable.summoningrituals.recipe.condition;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public final class ConditionStreamCodecs {

    public static final StreamCodec<RegistryFriendlyByteBuf, NumberProvider> NUMBER_PROVIDER_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(
        NumberProviders.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, IntRange> INT_RANGE_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(
        IntRange.CODEC);

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

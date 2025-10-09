package com.almostreliable.summoningrituals.recipe.output;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record EntityOutputInfo(Holder<EntityType<?>> entity, int count, Optional<CompoundTag> data) {

    public static final Codec<EntityOutputInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
        BuiltInRegistries.ENTITY_TYPE.holderByNameCodec().fieldOf("id").forGetter(EntityOutputInfo::entity),
        Codec.INT.fieldOf("count").forGetter(EntityOutputInfo::count),
        CompoundTag.CODEC.optionalFieldOf("data").forGetter(EntityOutputInfo::data)
    ).apply(i, EntityOutputInfo::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityOutputInfo> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.holderRegistry(Registries.ENTITY_TYPE), EntityOutputInfo::entity,
        ByteBufCodecs.VAR_INT, EntityOutputInfo::count,
        ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG), EntityOutputInfo::data,
        EntityOutputInfo::new
    );
}

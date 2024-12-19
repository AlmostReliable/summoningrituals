package com.almostreliable.summoningrituals.recipe.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public record EntitySpawn(Holder<EntityType<?>> entity, int count, Optional<CompoundTag> nbt) {

    public static final Codec<EntitySpawn> CODEC = RecordCodecBuilder.create(i -> i.group(
        BuiltInRegistries.ENTITY_TYPE.holderByNameCodec().fieldOf("id").forGetter(EntitySpawn::entity),
        Codec.INT.optionalFieldOf("count", 1).forGetter(EntitySpawn::count),
        CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(EntitySpawn::nbt)
    ).apply(i, EntitySpawn::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntitySpawn> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.holderRegistry(Registries.ENTITY_TYPE),
        EntitySpawn::entity,
        ByteBufCodecs.VAR_INT,
        EntitySpawn::count,
        ByteBufCodecs.COMPOUND_TAG,
        (e) -> e.nbt().orElse(new CompoundTag()),
        (entityTypeHolder, integer, compoundTag) -> new EntitySpawn(entityTypeHolder, integer, Optional.of(compoundTag))
    );
}

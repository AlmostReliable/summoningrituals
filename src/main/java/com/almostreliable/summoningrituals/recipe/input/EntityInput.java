package com.almostreliable.summoningrituals.recipe.input;

import com.almostreliable.summoningrituals.core.Constants;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.Predicate;

public record EntityInput(Holder<EntityType<?>> entityType, int count) implements Predicate<Entity> {

    public static final Codec<EntityInput> CODEC = RecordCodecBuilder.create(i -> i.group(
        BuiltInRegistries.ENTITY_TYPE.holderByNameCodec().fieldOf("id").forGetter(EntityInput::entityType),
        Codec.INT.fieldOf(Constants.COUNT).forGetter(EntityInput::count)
    ).apply(i, EntityInput::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityInput> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.holderRegistry(Registries.ENTITY_TYPE), EntityInput::entityType,
        ByteBufCodecs.VAR_INT, EntityInput::count,
        EntityInput::new
    );

    @Override
    public boolean test(Entity entity) {
        return entity.isAlive() && entityType.equals(entity.getType());
    }
}

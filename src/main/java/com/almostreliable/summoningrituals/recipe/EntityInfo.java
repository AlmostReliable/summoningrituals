package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.core.Constants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public record EntityInfo(
    Holder<EntityType<?>> entity, int count, Optional<CompoundTag> data, List<Component> tooltip
) implements Predicate<Entity> {

    public static final Codec<EntityInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
        BuiltInRegistries.ENTITY_TYPE.holderByNameCodec().fieldOf(Entity.ID_TAG).forGetter(EntityInfo::entity),
        Codec.INT.fieldOf(Constants.COUNT).forGetter(EntityInfo::count),
        CompoundTag.CODEC.optionalFieldOf(Constants.DATA).forGetter(EntityInfo::data),
        ComponentSerialization.CODEC.listOf().fieldOf(Constants.TOOLTIP).forGetter(EntityInfo::tooltip)
    ).apply(i, EntityInfo::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityInfo> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.holderRegistry(Registries.ENTITY_TYPE), EntityInfo::entity,
        ByteBufCodecs.VAR_INT, EntityInfo::count,
        ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG), EntityInfo::data,
        ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()), EntityInfo::tooltip,
        EntityInfo::new
    );

    /**
     * used for recipe matching, ignores data because data needs to be checked manually
     * see {@link AltarRecipe#getSacrifices(BlockPos, Function)}
     */
    @Override
    public boolean test(Entity entity) {
        return entity.isAlive() && this.entity.value().equals(entity.getType());
    }
}

package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.core.Constants;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.TriPredicate;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public record EntityInput(
    EntityInfo entityInfo, @Nullable Predicate<Entity> validator
) implements TriPredicate<ResourceLocation, Integer, Entity> {

    public static final Codec<EntityInput> CODEC = RecordCodecBuilder.create(i -> i.group(
        EntityInfo.CODEC.fieldOf(Constants.ENTITY).forGetter(EntityInput::entityInfo)
    ).apply(i, EntityInput::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityInput> STREAM_CODEC = StreamCodec.composite(
        EntityInfo.STREAM_CODEC, EntityInput::entityInfo,
        EntityInput::new
    );
    public static final Table<ResourceLocation, Integer, Predicate<Entity>> DATA_VALIDATORS = HashBasedTable.create();

    public EntityInput(EntityInfo entityInfo) {
        this(entityInfo, null);
    }

    @Override
    public boolean test(ResourceLocation recipeId, Integer inputIndex, Entity entity) {
        var predicate = DATA_VALIDATORS.get(recipeId, inputIndex);
        return entity.isAlive() && entityInfo.entity().value().equals(entity.getType()) && (predicate == null || predicate.test(entity));
    }
}

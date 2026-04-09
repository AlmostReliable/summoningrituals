package com.almostreliable.summoningrituals.recipe.input;

import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.container.EntityInfo;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.Predicate;

public record EntityInput(EntityInfo entityInfo, Predicate<Entity> predicate) implements BaseEntityInput {

    public static final Codec<EntityInput> CODEC = RecordCodecBuilder.create(i -> i.group(
        EntityInfo.CODEC.fieldOf(Constants.ENTITY).forGetter(EntityInput::entityInfo)
    ).apply(i, EntityInput::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityInput> STREAM_CODEC = StreamCodec.composite(
        EntityInfo.STREAM_CODEC, EntityInput::entityInfo,
        EntityInput::new
    );

    public EntityInput(EntityInfo entityInfo) {
        this(entityInfo, $ -> true);
    }

    @Override
    public boolean test(ResourceLocation recipeId, Integer inputIndex, Entity entity) {
        return entity.isAlive() && entityInfo.entity().value().equals(entity.getType()) &&
            test(DATA_VALIDATORS, recipeId, inputIndex, entity);
    }
}

package com.almostreliable.summoningrituals.recipe.output;

import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.EntityInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record EntityOutput(
    EntityInfo entityInfo, Optional<BlockPos> offset, Optional<BlockPos> spread
) implements RecipeOutput<Entity> {

    public static final Codec<EntityOutput> CODEC = RecordCodecBuilder.create(i -> i.group(
        EntityInfo.CODEC.fieldOf(Constants.ENTITY).forGetter(EntityOutput::entityInfo),
        BlockPos.CODEC.optionalFieldOf(Constants.OFFSET).forGetter(EntityOutput::offset),
        BlockPos.CODEC.optionalFieldOf(Constants.SPREAD).forGetter(EntityOutput::spread)
    ).apply(i, EntityOutput::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityOutput> STREAM_CODEC = StreamCodec.composite(
        EntityInfo.STREAM_CODEC, EntityOutput::entityInfo,
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC), EntityOutput::offset,
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC), EntityOutput::spread,
        EntityOutput::new
    );

    @Override
    public Collection<Entity> spawn(ServerLevel level, BlockPos origin) {
        var result = new ArrayList<Entity>();
        var toSpawn = entityInfo.count();

        while (toSpawn > 0) {
            var entity = entityInfo.entityInfo().value().create(level);
            if (entity == null) return List.of();

            var pos = getRandomPos(origin);
            entity.setPos(pos);
            entityInfo.data().ifPresent(data -> {
                var newData = new CompoundTag();
                entity.saveWithoutId(newData);
                newData.merge(data);
                entity.load(newData);
            });
            level.addFreshEntity(entity);

            result.add(entity);
            toSpawn--;
        }

        return result;
    }
}

package com.almostreliable.summoningrituals.recipe.output;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record EntityOutput(
    EntityOutputInfo entityInfo, Optional<BlockPos> offset, Optional<BlockPos> spread
) implements RecipeOutput<Entity> {

    public static final Codec<EntityOutput> CODEC = RecordCodecBuilder.create(i -> i.group(
        EntityOutputInfo.CODEC.fieldOf("entity").forGetter(EntityOutput::entityInfo),
        BlockPos.CODEC.optionalFieldOf("offset").forGetter(EntityOutput::offset),
        BlockPos.CODEC.optionalFieldOf("spread").forGetter(EntityOutput::spread)
    ).apply(i, EntityOutput::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityOutput> STREAM_CODEC = StreamCodec.composite(
        EntityOutputInfo.STREAM_CODEC, EntityOutput::entityInfo,
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC), EntityOutput::offset,
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC), EntityOutput::spread,
        EntityOutput::new
    );

    @Override
    public Collection<Entity> spawn(ServerLevel level, BlockPos origin) {
        var result = new ArrayList<Entity>();
        var toSpawn = entityInfo.count();

        while (toSpawn > 0) {
            var entity = entityInfo.entity().value().create(level);
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

    public static class Builder {

        private final Holder<EntityType<?>> entity;
        private final int count;
        @Nullable
        private CompoundTag data;
        @Nullable
        private BlockPos offset;
        @Nullable
        private BlockPos spread;

        public Builder(Holder<EntityType<?>> entity) {
            this(entity, 1);
        }

        public Builder(Holder<EntityType<?>> entity, int count) {
            this.entity = entity;
            this.count = count;
        }

        public Builder data(CompoundTag data) {
            this.data = data;
            return this;
        }

        public Builder offset(BlockPos offset) {
            this.offset = offset;
            return this;
        }

        public Builder spread(BlockPos spread) {
            this.spread = spread;
            return this;
        }

        public EntityOutput build() {
            var entitySpawn = new EntityOutputInfo(entity, count, Optional.ofNullable(data));
            return new EntityOutput(entitySpawn, Optional.ofNullable(offset), Optional.ofNullable(spread));
        }
    }
}

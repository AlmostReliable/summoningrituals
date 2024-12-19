package com.almostreliable.summoningrituals.recipe.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.Optional;

public record EntityOutput(EntitySpawn entity, Optional<BlockPos> offset, Optional<BlockPos> spread) implements RecipeOutput {

    public static final Codec<EntityOutput> CODEC = RecordCodecBuilder.create(i -> i.group(
        EntitySpawn.CODEC.fieldOf("entity").forGetter(EntityOutput::entity),
        BlockPos.CODEC.optionalFieldOf("offset").forGetter(EntityOutput::offset),
        BlockPos.CODEC.optionalFieldOf("spread").forGetter(EntityOutput::spread)
    ).apply(i, EntityOutput::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityOutput> STREAM_CODEC = StreamCodec.composite(
        EntitySpawn.STREAM_CODEC,
        EntityOutput::entity,
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
        EntityOutput::offset,
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
        EntityOutput::spread,
        EntityOutput::new
    );

    @Override
    public void spawn(ServerLevel level, BlockPos origin) {

        for (var i = 0; i < entity.count(); i++) {
            var mobEntity = entity.entity().value().create(level);
            if (mobEntity == null) return;
            var pos = getRandomPos(origin);
            mobEntity.setPos(pos.x(), pos.y(), pos.z());
            entity.nbt().ifPresent(nbt -> {
                var newNbt = new CompoundTag();
                mobEntity.saveWithoutId(newNbt);
                newNbt.merge(nbt);
                mobEntity.load(newNbt);
            });

            level.addFreshEntity(mobEntity);
        }
    }

    public static class Builder {

        private final Holder<EntityType<?>> entity;
        private final int count;
        @Nullable
        private BlockPos offset;
        @Nullable
        private BlockPos spread;
        @Nullable
        private CompoundTag nbt;

        public Builder(Holder<EntityType<?>> entity) {
            this(entity, 1);
        }

        public Builder(Holder<EntityType<?>> entity, int count) {
            this.entity = entity;
            this.count = count;
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
            var entitySpawn = new EntitySpawn(entity, count, Optional.ofNullable(nbt));
            return new EntityOutput(entitySpawn, Optional.ofNullable(offset), Optional.ofNullable(spread));
        }
    }
}

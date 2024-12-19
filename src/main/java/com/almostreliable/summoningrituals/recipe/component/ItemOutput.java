package com.almostreliable.summoningrituals.recipe.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public record ItemOutput(ItemStack item, Optional<BlockPos> offset, Optional<BlockPos> spread) implements RecipeOutput {

    public static final Codec<ItemOutput> CODEC = RecordCodecBuilder.create(i -> i.group(
        ItemStack.STRICT_CODEC.fieldOf("item").forGetter(ItemOutput::item),
        BlockPos.CODEC.optionalFieldOf("offset").forGetter(ItemOutput::offset),
        BlockPos.CODEC.optionalFieldOf("spread").forGetter(ItemOutput::spread)
    ).apply(i, ItemOutput::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemOutput> STREAM_CODEC = StreamCodec.composite(
        ItemStack.STREAM_CODEC,
        ItemOutput::item,
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
        ItemOutput::offset,
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
        ItemOutput::spread,
        ItemOutput::new
    );

    @Override
    public void spawn(ServerLevel level, BlockPos origin) {
        var toSpawn = item.getCount();
        while (toSpawn > 0) {
            var stack = item.copyWithCount(Math.min(toSpawn, 4));
            var pos = getRandomPos(origin);
            var entity = new ItemEntity(level, pos.x(), pos.y(), pos.z(), stack);
            level.addFreshEntity(entity);
            toSpawn -= stack.getCount();
        }
    }

    public static class Builder {

        private final ItemStack item;
        @Nullable
        private BlockPos offset;
        @Nullable
        private BlockPos spread;

        public Builder(ItemStack item) {
            this.item = item;
        }

        public Builder offset(BlockPos offset) {
            this.offset = offset;
            return this;
        }

        public Builder spread(BlockPos spread) {
            this.spread = spread;
            return this;
        }

        public ItemOutput build() {
            return new ItemOutput(item, Optional.ofNullable(offset), Optional.ofNullable(spread));
        }
    }
}

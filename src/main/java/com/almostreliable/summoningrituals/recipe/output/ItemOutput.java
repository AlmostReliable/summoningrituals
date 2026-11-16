package com.almostreliable.summoningrituals.recipe.output;

import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.util.CodecUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStackTemplate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public record ItemOutput(ItemStackTemplate item, Optional<BlockPos> offset, Optional<BlockPos> spread) implements RecipeOutput<ItemEntity> {

    public static final Codec<ItemOutput> CODEC = RecordCodecBuilder.create(i -> i.group(
        CodecUtils.STRICT_ITEM_TEMPLATE_CODEC.fieldOf(Constants.ITEM).forGetter(ItemOutput::item),
        BlockPos.CODEC.optionalFieldOf(Constants.OFFSET).forGetter(ItemOutput::offset),
        BlockPos.CODEC.optionalFieldOf(Constants.SPREAD).forGetter(ItemOutput::spread)
    ).apply(i, ItemOutput::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemOutput> STREAM_CODEC = StreamCodec.composite(
        ItemStackTemplate.STREAM_CODEC, ItemOutput::item,
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC), ItemOutput::offset,
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC), ItemOutput::spread,
        ItemOutput::new
    );

    @Override
    public Collection<ItemEntity> spawn(ServerLevel level, BlockPos origin) {
        var result = new ArrayList<ItemEntity>();
        var toSpawn = item.count();

        while (toSpawn > 0) {
            var stack = item.withCount(Math.min(toSpawn, 4)).create();
            var pos = getRandomPos(origin);
            var entity = new ItemEntity(level, pos.x(), pos.y(), pos.z(), stack);
            level.addFreshEntity(entity);
            result.add(entity);
            toSpawn -= stack.getCount();
        }

        return result;
    }
}

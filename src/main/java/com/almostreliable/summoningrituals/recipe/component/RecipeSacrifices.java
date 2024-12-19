package com.almostreliable.summoningrituals.recipe.component;

import com.almostreliable.summoningrituals.Constants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public record RecipeSacrifices(List<Sacrifice> sacrifices, BlockPos region) {

    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeSacrifices> STREAM_CODEC = StreamCodec.composite(
        Sacrifice.STREAM_CODEC.apply(ByteBufCodecs.list()),
        RecipeSacrifices::sacrifices,
        BlockPos.STREAM_CODEC,
        RecipeSacrifices::region,
        RecipeSacrifices::new
    );
    private static final BlockPos DEFAULT_ZONE = new BlockPos(3, 2, 3);
    public static final Codec<RecipeSacrifices> CODEC = RecordCodecBuilder.create(i -> i.group(
        Sacrifice.CODEC.listOf().fieldOf("entities").forGetter(RecipeSacrifices::sacrifices),
        BlockPos.CODEC.optionalFieldOf("region", DEFAULT_ZONE).forGetter(RecipeSacrifices::region)
    ).apply(i, RecipeSacrifices::new));

    public void add(Holder<EntityType<?>> mob, int count) {
        sacrifices.add(new Sacrifice(mob, count));
    }

    public AABB getRegion(BlockPos pos) {
        BlockPos startBounds = pos.offset(region.multiply(-1));
        BlockPos endBounds = pos.offset(region);
        return new AABB(
            new Vec3(startBounds.getX(), startBounds.getY(), startBounds.getZ()),
            new Vec3(endBounds.getX(), endBounds.getY(), endBounds.getZ())
        );
    }

    public boolean test(Predicate<? super Sacrifice> predicate) {
        return sacrifices.stream().allMatch(predicate);
    }

    public int size() {
        return sacrifices.size();
    }

    public Sacrifice get(int index) {
        return sacrifices.get(index);
    }

    public String getDisplayRegion() {
        return String.format("%dx%dx%d", region.getX(), region.getY(), region.getZ());
    }

    public boolean isEmpty() {
        return sacrifices.isEmpty();
    }

    public record Sacrifice(Holder<EntityType<?>> entity, int count) {

        public static final Codec<Sacrifice> CODEC = RecordCodecBuilder.create(i -> i.group(
            BuiltInRegistries.ENTITY_TYPE.holderByNameCodec().fieldOf("entity").forGetter(Sacrifice::entity),
            Codec.INT.optionalFieldOf(Constants.COUNT, 1).forGetter(Sacrifice::count)
        ).apply(i, Sacrifice::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Sacrifice> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ENTITY_TYPE),
            Sacrifice::entity,
            ByteBufCodecs.VAR_INT,
            Sacrifice::count,
            Sacrifice::new
        );

        public boolean test(EntityType<?> type) {
            return entity().value().equals(type);
        }
    }
}

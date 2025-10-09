package com.almostreliable.summoningrituals.recipe.input;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.rhino.util.HideFromJS;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record EntityInputs(List<EntityInput> inputs, BlockPos zone) {

    public static final BlockPos DEFAULT_ZONE = new BlockPos(3, 2, 3);
    public static final EntityInputs EMPTY = new EntityInputs(List.of(), DEFAULT_ZONE);

    public static final Codec<EntityInputs> CODEC = RecordCodecBuilder.create(i -> i.group(
        EntityInput.CODEC.listOf().fieldOf("inputs").forGetter(EntityInputs::inputs),
        BlockPos.CODEC.optionalFieldOf("zone", DEFAULT_ZONE).forGetter(EntityInputs::zone)
    ).apply(i, EntityInputs::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityInputs> STREAM_CODEC = StreamCodec.composite(
        EntityInput.STREAM_CODEC.apply(ByteBufCodecs.list()), EntityInputs::inputs,
        BlockPos.STREAM_CODEC, EntityInputs::zone,
        EntityInputs::new
    );

    @Nullable
    public List<Entity> getSacrifices(BlockPos pos, Function<AABB, List<Entity>> entityCollector) {
        if (isEmpty()) return List.of();

        var region = constructRegion(pos);
        var entities = entityCollector.apply(region);
        var sacrifices = new ArrayList<Entity>();

        for (var input : inputs) {
            var requiredCount = input.count();
            var matchingEntities = entities.stream().filter(input).toList();

            if (matchingEntities.size() < requiredCount) return null;

            sacrifices.addAll(matchingEntities.subList(0, requiredCount));
        }

        return sacrifices;
    }

    private AABB constructRegion(BlockPos pos) {
        var startBounds = pos.offset(zone.multiply(-1));
        var endBounds = pos.offset(zone);
        return new AABB(
            new Vec3(startBounds.getX(), startBounds.getY(), startBounds.getZ()),
            new Vec3(endBounds.getX(), endBounds.getY(), endBounds.getZ())
        );
    }

    public boolean isEmpty() {
        return inputs.isEmpty();
    }

    public static class Builder {

        private final List<EntityInput> inputs = new ArrayList<>();
        private final BlockPos zone;

        public Builder(BlockPos zone) {
            this.zone = zone;
        }

        public Builder() {
            this(DEFAULT_ZONE);
        }

        public Builder add(Holder<EntityType<?>> entityType, int count) {
            inputs.add(new EntityInput(entityType, count));
            return this;
        }

        public Builder add(Holder<EntityType<?>> entityType) {
            add(entityType, 1);
            return this;
        }

        public Builder addAll(HolderSet<EntityType<?>> entityTypes) {
            entityTypes.forEach(this::add);
            return this;
        }

        @HideFromJS
        public EntityInputs build() {
            return new EntityInputs(inputs, zone);
        }
    }
}

package com.almostreliable.summoningrituals.compat.kubejs.builder;

import com.almostreliable.summoningrituals.recipe.output.EntityOutput;
import com.almostreliable.summoningrituals.recipe.output.EntityOutputInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EntityOutputBuilder {

    private final Holder<EntityType<?>> entity;
    private final int count;
    @Nullable
    private CompoundTag data;
    @Nullable
    private BlockPos offset;
    @Nullable
    private BlockPos spread;

    public EntityOutputBuilder(Holder<EntityType<?>> entity) {
        this(entity, 1);
    }

    public EntityOutputBuilder(Holder<EntityType<?>> entity, int count) {
        this.entity = entity;
        this.count = count;
    }

    public EntityOutputBuilder data(CompoundTag data) {
        this.data = data;
        return this;
    }

    public EntityOutputBuilder offset(BlockPos offset) {
        this.offset = offset;
        return this;
    }

    public EntityOutputBuilder spread(BlockPos spread) {
        this.spread = spread;
        return this;
    }

    public EntityOutput build() {
        var entitySpawn = new EntityOutputInfo(entity, count, Optional.ofNullable(data));
        return new EntityOutput(entitySpawn, Optional.ofNullable(offset), Optional.ofNullable(spread));
    }
}

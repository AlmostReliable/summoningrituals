package com.almostreliable.summoningrituals.compat.kubejs.builder;

import com.almostreliable.summoningrituals.recipe.EntityInfo;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

import dev.latvian.mods.rhino.util.HideFromJS;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SummoningEntityBuilder {

    private final Holder<EntityType<?>> entity;
    private final int count;
    @Nullable
    private CompoundTag data;
    @Nullable
    private BlockPos offset;
    @Nullable
    private BlockPos spread;

    public SummoningEntityBuilder(Holder<EntityType<?>> entity) {
        this(entity, 1);
    }

    public SummoningEntityBuilder(Holder<EntityType<?>> entity, int count) {
        this.entity = entity;
        this.count = count;
    }

    public SummoningEntityBuilder data(CompoundTag data) {
        this.data = data;
        return this;
    }

    public SummoningEntityBuilder offset(BlockPos offset) {
        this.offset = offset;
        return this;
    }

    public SummoningEntityBuilder spread(BlockPos spread) {
        this.spread = spread;
        return this;
    }

    @HideFromJS
    public EntityInfo buildEntityInfo() {
        return new EntityInfo(entity, count, Optional.ofNullable(data));
    }

    public EntityOutput build() {
        var entityInfo = buildEntityInfo();
        return new EntityOutput(entityInfo, Optional.ofNullable(offset), Optional.ofNullable(spread));
    }
}

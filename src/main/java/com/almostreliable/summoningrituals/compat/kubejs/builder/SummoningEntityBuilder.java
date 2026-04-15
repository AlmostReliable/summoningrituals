package com.almostreliable.summoningrituals.compat.kubejs.builder;

import com.almostreliable.summoningrituals.recipe.container.EntityInfo;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SummoningEntityBuilder {

    private final Holder<EntityType<?>> entity;
    private final int count;
    @Nullable
    private CompoundTag data;
    private final List<Component> tooltip = new ArrayList<>();

    public SummoningEntityBuilder(Holder<EntityType<?>> entity) {
        this(entity, 1);
    }

    public SummoningEntityBuilder(Holder<EntityType<?>> entity, int count) {
        this.entity = entity;
        this.count = count;
    }

    public SummoningEntityBuilder(EntityInfo entity) {
        this(entity.entity(), entity.count());
    }

    @ReturnsSelf
    public SummoningEntityBuilder data(CompoundTag data) {
        this.data = data;
        return this;
    }

    @ReturnsSelf
    public SummoningEntityBuilder tooltip(List<Component> tooltip) {
        this.tooltip.addAll(tooltip);
        return this;
    }

    @HideFromJS
    public EntityInfo build() {
        return new EntityInfo(entity, count, Optional.ofNullable(data), tooltip);
    }
}

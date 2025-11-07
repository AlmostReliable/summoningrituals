package com.almostreliable.summoningrituals.compat.kubejs.binding;

import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityBuilder;
import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityOutputBuilder;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

public interface SummoningEntityBinding {

    static SummoningEntityBuilder input(Holder<EntityType<?>> entity) {
        return new SummoningEntityBuilder(entity);
    }

    static SummoningEntityBuilder input(Holder<EntityType<?>> entity, int count) {
        return new SummoningEntityBuilder(entity, count);
    }

    static SummoningEntityBuilder input(Holder<EntityType<?>> entity, int count, CompoundTag data) {
        return new SummoningEntityBuilder(entity, count).data(data);
    }

    static SummoningEntityOutputBuilder output(Holder<EntityType<?>> entity) {
        return new SummoningEntityOutputBuilder(entity);
    }

    static SummoningEntityOutputBuilder output(Holder<EntityType<?>> entity, int count) {
        return new SummoningEntityOutputBuilder(entity, count);
    }
}

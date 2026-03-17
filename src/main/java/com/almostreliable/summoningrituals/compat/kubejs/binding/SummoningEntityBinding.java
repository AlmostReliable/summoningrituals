package com.almostreliable.summoningrituals.compat.kubejs.binding;

import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityInputBuilder;
import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityOutputBuilder;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;

public interface SummoningEntityBinding {

    static SummoningEntityInputBuilder input(Holder<EntityType<?>> entity) {
        return new SummoningEntityInputBuilder(entity);
    }

    static SummoningEntityInputBuilder input(Holder<EntityType<?>> entity, int count) {
        return new SummoningEntityInputBuilder(entity, count);
    }

    static SummoningEntityOutputBuilder output(Holder<EntityType<?>> entity) {
        return new SummoningEntityOutputBuilder(entity);
    }

    static SummoningEntityOutputBuilder output(Holder<EntityType<?>> entity, int count) {
        return new SummoningEntityOutputBuilder(entity, count);
    }
}

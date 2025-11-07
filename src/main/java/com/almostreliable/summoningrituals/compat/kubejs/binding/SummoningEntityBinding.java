package com.almostreliable.summoningrituals.compat.kubejs.binding;

import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityBuilder;
import com.almostreliable.summoningrituals.recipe.EntityInfo;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

public interface SummoningEntityBinding {

    static EntityInfo input(Holder<EntityType<?>> entity) {
        return new SummoningEntityBuilder(entity).buildEntityInfo();
    }

    static EntityInfo input(Holder<EntityType<?>> entity, int count) {
        return new SummoningEntityBuilder(entity, count).buildEntityInfo();
    }

    static EntityInfo input(Holder<EntityType<?>> entity, int count, CompoundTag data) {
        return new SummoningEntityBuilder(entity, count).data(data).buildEntityInfo();
    }

    static SummoningEntityBuilder output(Holder<EntityType<?>> entity) {
        return new SummoningEntityBuilder(entity);
    }

    static SummoningEntityBuilder output(Holder<EntityType<?>> entity, int count) {
        return new SummoningEntityBuilder(entity, count);
    }

    static SummoningEntityBuilder output(Holder<EntityType<?>> entity, int count, CompoundTag data) {
        return new SummoningEntityBuilder(entity, count).data(data);
    }
}

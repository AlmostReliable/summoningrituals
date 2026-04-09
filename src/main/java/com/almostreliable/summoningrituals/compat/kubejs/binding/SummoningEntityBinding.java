package com.almostreliable.summoningrituals.compat.kubejs.binding;

import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityInputBuilder;
import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityOutputBuilder;
import com.almostreliable.summoningrituals.recipe.container.EntityInfo;
import com.almostreliable.summoningrituals.recipe.input.FakeEntityInput;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Predicate;

public interface SummoningEntityBinding {

    static SummoningEntityInputBuilder input(EntityInfo entity) {
        return new SummoningEntityInputBuilder(entity);
    }

    static SummoningEntityInputBuilder input(Holder<EntityType<?>> entity, int count) {
        return new SummoningEntityInputBuilder(entity, count);
    }

    static FakeEntityInput fakeInput(ItemStack displayItem, int count, Predicate<Entity> predicate) {
        return new FakeEntityInput(displayItem, Optional.of(count), predicate);
    }

    static FakeEntityInput fakeInput(ItemStack displayItem, Predicate<Entity> predicate) {
        return new FakeEntityInput(displayItem, Optional.empty(), predicate);
    }

    static SummoningEntityOutputBuilder output(EntityInfo entity) {
        return new SummoningEntityOutputBuilder(entity);
    }

    static SummoningEntityOutputBuilder output(Holder<EntityType<?>> entity, int count) {
        return new SummoningEntityOutputBuilder(entity, count);
    }
}

package com.almostreliable.summoningrituals.compat.kubejs.binding;

import com.almostreliable.summoningrituals.compat.kubejs.builder.EntityOutputBuilder;
import com.almostreliable.summoningrituals.compat.kubejs.builder.ItemOutputBuilder;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

public interface SummoningOutputBinding {

    static ItemOutputBuilder itemOutput(ItemStack stack) {
        return new ItemOutputBuilder(stack);
    }

    static EntityOutputBuilder entityOutput(Holder<EntityType<?>> entity) {
        return new EntityOutputBuilder(entity);
    }

    static EntityOutputBuilder entityOutput(Holder<EntityType<?>> entity, int count) {
        return new EntityOutputBuilder(entity, count);
    }

    static EntityOutput entityOutput(Holder<EntityType<?>> entity, int count, CompoundTag data) {
        return new EntityOutputBuilder(entity, count).data(data).build();
    }
}

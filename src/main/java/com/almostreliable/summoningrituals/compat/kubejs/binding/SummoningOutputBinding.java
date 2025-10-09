package com.almostreliable.summoningrituals.compat.kubejs.binding;

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

    static EntityOutput.Builder entityOutput(Holder<EntityType<?>> entity) {
        return new EntityOutput.Builder(entity);
    }

    static EntityOutput.Builder entityOutput(Holder<EntityType<?>> entity, int count) {
        return new EntityOutput.Builder(entity, count);
    }

    static EntityOutput entityOutput(Holder<EntityType<?>> entity, int count, CompoundTag data) {
        return new EntityOutput.Builder(entity, count).data(data).build();
    }
}

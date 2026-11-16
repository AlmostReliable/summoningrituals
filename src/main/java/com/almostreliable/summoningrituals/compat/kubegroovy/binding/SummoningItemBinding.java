package com.almostreliable.summoningrituals.compat.kubegroovy.binding;

import com.almostreliable.summoningrituals.compat.kubegroovy.builder.ItemOutputBuilder;

import net.minecraft.world.item.ItemStack;

public interface SummoningItemBinding {

    static ItemOutputBuilder of(ItemStack stack) {
        return new ItemOutputBuilder(stack);
    }

    static ItemOutputBuilder of(ItemStack stack, int count) {
        stack.setCount(count);
        return of(stack);
    }
}

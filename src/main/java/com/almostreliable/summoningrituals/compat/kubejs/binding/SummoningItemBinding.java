package com.almostreliable.summoningrituals.compat.kubejs.binding;

import com.almostreliable.summoningrituals.compat.kubejs.builder.ItemOutputBuilder;

import net.minecraft.world.item.ItemStack;

public interface SummoningItemBinding {

    static ItemOutputBuilder of(ItemStack stack) {
        return new ItemOutputBuilder(stack);
    }
}

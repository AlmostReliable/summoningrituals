package com.almostreliable.summoningrituals.compat.kubejs.builder;

import com.almostreliable.summoningrituals.recipe.output.ItemOutput;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.rhino.util.HideFromJS;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ItemOutputBuilder {

    private final ItemStack item;
    @Nullable
    private BlockPos offset;
    @Nullable
    private BlockPos spread;

    public ItemOutputBuilder(ItemStack item) {
        this.item = item;
    }

    public ItemOutputBuilder offset(BlockPos offset) {
        this.offset = offset;
        return this;
    }

    public ItemOutputBuilder spread(BlockPos spread) {
        this.spread = spread;
        return this;
    }

    @HideFromJS
    public ItemOutput build() {
        return new ItemOutput(item, Optional.ofNullable(offset), Optional.ofNullable(spread));
    }
}

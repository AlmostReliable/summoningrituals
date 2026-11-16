package com.almostreliable.summoningrituals.compat.kubegroovy.builder;

import com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component.ItemOutputComponent;
import com.almostreliable.summoningrituals.recipe.output.ItemOutput;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import dev.uncandango.kubegroovy.script.annotations.HideFromGY;
import dev.uncandango.kubegroovy.script.annotations.ReturnsSelf;

import org.jspecify.annotations.Nullable;

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

    @ReturnsSelf
    public ItemOutputBuilder offset(BlockPos offset) {
        this.offset = offset;
        return this;
    }

    @ReturnsSelf
    public ItemOutputBuilder spread(BlockPos spread) {
        this.spread = spread;
        return this;
    }

    @HideFromGY
    public ItemOutput build() {
        return new ItemOutput(ItemOutputComponent.templateOf(item), Optional.ofNullable(offset), Optional.ofNullable(spread));
    }
}

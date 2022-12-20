package com.almostreliable.summoningrituals.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.List;

/**
 * A wrapper class to delegate {@link Container} methods to {@link ItemHandler}.
 */
public class VanillaWrapper extends RecipeWrapper {

    private final ItemHandler delegate;

    VanillaWrapper(ItemHandler delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    public List<ItemStack> getItems() {
        return delegate.noneEmptyItems;
    }

    public ItemStack getCatalyst() {
        return delegate.catalyst;
    }
}

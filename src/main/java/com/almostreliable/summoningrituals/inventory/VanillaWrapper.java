package com.almostreliable.summoningrituals.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A wrapper class to delegate {@link Container} methods to {@link ItemHandler}.
 * <p>
 * Taken from Minecraft Forge's {@code RecipeWrapper}.
 */
public class VanillaWrapper implements Container {

    private final ItemHandler delegate;

    VanillaWrapper(ItemHandler delegate) {
        this.delegate = delegate;
    }

    public List<ItemStack> getItems() {
        return delegate.getNoneEmptyItems();
    }

    public ItemStack getCatalyst() {
        return delegate.getCatalyst();
    }

    @Override
    public int getContainerSize() {
        return delegate.getSlots();
    }

    @Override
    public ItemStack getItem(int slot) {
        return delegate.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = delegate.getStackInSlot(slot);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.split(count);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        delegate.setStackInSlot(slot, stack);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        var stack = getItem(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        setItem(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < delegate.getSlots(); slot++) {
            if (!delegate.getStackInSlot(slot).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return delegate.isItemValid(slot, stack);
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < delegate.getSlots(); slot++) {
            delegate.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void startOpen(Player player) {}

    @Override
    public void stopOpen(Player player) {}
}

package com.almostreliable.summoningrituals.altar.inventory;

import com.almostreliable.summoningrituals.core.Constants;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.Iterator;

final class InternalInventory implements INBTSerializable<ListTag>, Iterable<ItemStack> {

    private ItemStack[] items;

    InternalInventory(int size) {
        this.items = new ItemStack[size];
        clear();
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return Arrays.stream(items).iterator();
    }

    void set(int slot, ItemStack stack) {
        items[slot] = stack;
    }

    ItemStack get(int slot) {
        return items[slot];
    }

    void remove(int slot) {
        items[slot] = ItemStack.EMPTY;
    }

    void clear() {
        Arrays.fill(items, ItemStack.EMPTY);
    }

    int size() {
        return items.length;
    }

    @Override
    @UnknownNullability
    public ListTag serializeNBT(HolderLookup.Provider provider) {
        var itemsListTag = new ListTag();

        for (var i = 0; i < items.length; i++) {
            if (items[i].isEmpty()) continue;
            var itemTag = new CompoundTag();
            itemTag.putInt(Constants.SLOT, i);
            var finishedItemTag = items[i].save(provider, itemTag);
            itemsListTag.add(finishedItemTag);
        }

        return itemsListTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, ListTag listTag) {
        items = new ItemStack[listTag.size()];

        for (var i = 0; i < listTag.size(); i++) {
            var itemTag = listTag.getCompound(i);
            var slot = itemTag.getInt(Constants.SLOT);
            ItemStack.parse(provider, itemTag).ifPresent(stack -> items[slot] = stack);
        }
    }
}

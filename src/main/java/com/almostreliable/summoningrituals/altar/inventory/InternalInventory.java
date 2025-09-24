package com.almostreliable.summoningrituals.altar.inventory;

import com.almostreliable.summoningrituals.core.Constants;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.Iterator;

final class InternalInventory implements INBTSerializable<CompoundTag>, Iterable<ItemStack> {

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
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var itemsListTag = new ListTag();

        for (var i = 0; i < items.length; i++) {
            if (items[i].isEmpty()) continue;
            var itemTag = new CompoundTag();
            itemTag.putInt(Constants.SLOT, i);
            var finishedItemTag = items[i].save(provider, itemTag);
            itemsListTag.add(finishedItemTag);
        }

        var compoundTag = new CompoundTag();
        compoundTag.putInt(Constants.SIZE, items.length);
        compoundTag.put(Constants.ITEMS, itemsListTag);

        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        var size = tag.getInt(Constants.SIZE);
        var itemsListTag = tag.getList(Constants.ITEMS, Tag.TAG_COMPOUND);

        items = new ItemStack[size];
        clear();

        for (var i = 0; i < itemsListTag.size(); i++) {
            var itemTag = itemsListTag.getCompound(i);
            var slot = itemTag.getInt(Constants.SLOT);
            ItemStack.parse(provider, itemTag).ifPresent(stack -> items[slot] = stack);
        }
    }
}

package com.almostreliable.summoningrituals.altar.inventory;

import com.almostreliable.summoningrituals.core.Constants;

import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Arrays;
import java.util.Iterator;

final class InternalInventory implements Iterable<ItemStack> {

    static final Codec<Tuple<ItemStack, Integer>> SLOTTED_STACK_CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.fieldOf(Constants.SLOT).forGetter(Tuple::getB),
        ItemStack.MAP_CODEC.forGetter(Tuple::getA)
    ).apply(i, (slot, stack) -> new Tuple<>(stack, slot)));

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

    int slots() {
        return items.length;
    }

    ItemStack[] snapshot() {
        var snapshot = new ItemStack[items.length];

        for (var i = 0; i < items.length; i++) {
            snapshot[i] = items[i].copy();
        }

        return snapshot;
    }

    void restore(ItemStack... snapshot) {
        items = snapshot;
    }

    void save(ValueOutput output) {
        var itemsOutput = output.list(Constants.ITEMS, SLOTTED_STACK_CODEC);

        for (var i = 0; i < items.length; i++) {
            if (items[i].isEmpty()) continue;

            var slottedStack = new Tuple<>(items[i], i);
            itemsOutput.add(slottedStack);
        }

        output.putInt(Constants.SIZE, items.length);
    }

    void load(ValueInput input) {
        var size = input.getIntOr(Constants.SIZE, 0);

        items = new ItemStack[size];
        clear();

        var slottedStacks = input.listOrEmpty(Constants.ITEMS, SLOTTED_STACK_CODEC);
        for (var entry : slottedStacks) {
            items[entry.getB()] = entry.getA();
        }
    }
}

package com.almostreliable.summoningrituals.inventory;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.AltarRecipeSerializer;
import com.almostreliable.summoningrituals.util.GameUtils;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class AltarInventory implements IItemHandlerModifiable, RecipeInput, INBTSerializable<CompoundTag> {

    private final AltarBlockEntity altar;
    private final InternalInventory inventory;
    private final Deque<Tuple<ItemStack, Integer>> insertOrder;

    private ItemStack catalyst;

    public AltarInventory(AltarBlockEntity altar) {
        this.altar = altar;
        int size = Config.COMMON.altarInventorySize.get();
        this.inventory = new InternalInventory(size);
        this.insertOrder = new ArrayDeque<>(size);
        this.catalyst = ItemStack.EMPTY;
    }

    @Override
    @UnknownNullability
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ListTag inventoryTag = inventory.serializeNBT(provider);

        ListTag insertOrderTag = new ListTag();
        for (var entry : insertOrder) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt(Constants.SLOT, entry.getB());
            Tag finishedEntryTag = entry.getA().save(provider, entryTag);
            insertOrderTag.add(finishedEntryTag);
        }

        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put(Constants.INVENTORY, inventoryTag);
        compoundTag.put(Constants.INSERT_ORDER, insertOrderTag);
        compoundTag.put(Constants.CATALYST, catalyst.saveOptional(provider));

        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        ListTag inventoryTag = tag.getList(Constants.INVENTORY, Tag.TAG_COMPOUND);
        inventory.deserializeNBT(provider, inventoryTag);

        ListTag insertOrderTag = tag.getList(Constants.INSERT_ORDER, Tag.TAG_COMPOUND);
        insertOrder.clear();
        for (int i = 0; i < insertOrderTag.size(); i++) {
            CompoundTag entryTag = insertOrderTag.getCompound(i);
            int slot = entryTag.getInt(Constants.SLOT);
            ItemStack.parse(provider, entryTag).ifPresent(stack -> insertOrder.add(new Tuple<>(stack, slot)));
        }

        CompoundTag catalystTag = tag.getCompound(Constants.CATALYST);
        catalyst = ItemStack.parseOptional(provider, catalystTag);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot == inventory.size()) {
            catalyst = stack;
            return;
        }

        inventory.set(slot, stack);
        onContentsChanged();
    }

    public ItemStack handleInsertion(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        var remaining = stack.copy();
        for (var i = 0; i < AltarRecipeSerializer.MAX_INPUTS; i++) {
            var original = remaining.copy();
            remaining = insertItem(i, remaining);

            if (remaining.isEmpty()) {
                insertOrder.push(new Tuple<>(original, i));
                return ItemStack.EMPTY;
            }

            if (remaining.getCount() == original.getCount()) {
                continue;
            }

            original.shrink(remaining.getCount());
            insertOrder.push(new Tuple<>(original, i));
        }

        return remaining;
    }

    public void popLastInserted() {
        var level = altar.getLevel();
        assert level != null && !level.isClientSide;

        if (!catalyst.isEmpty()) {
            GameUtils.dropItem(level, altar.getBlockPos(), catalyst, true);
            catalyst = ItemStack.EMPTY;
            onContentsChanged();
            return;
        }

        if (insertOrder.isEmpty()) return;

        var last = insertOrder.pop();
        var stack = last.getA();
        int slot = last.getB();

        inventory.get(slot).shrink(stack.getCount());
        if (inventory.get(slot).isEmpty()) {
            inventory.set(slot, ItemStack.EMPTY);
        }
        onContentsChanged();

        GameUtils.dropItem(level, altar.getBlockPos(), stack, true);
    }

    public void dropContents() {
        var level = altar.getLevel();
        assert level != null && !level.isClientSide;

        var pos = altar.getBlockPos();
        for (var stack : inventory) {
            if (stack.isEmpty()) continue;
            GameUtils.dropItem(level, pos, stack, false);
        }

        if (!catalyst.isEmpty()) {
            GameUtils.dropItem(level, pos, catalyst, false);
        }
    }

    public boolean handleRecipe(AltarRecipe recipe) {
        var itemBackup = createItemBackup();

        var toRemove = 0;
        var actualRemoved = 0;

        for (var input : recipe.inputs()) {
            toRemove += input.count();
            var inputRemoved = 0;

            for (var stack : inventory) {
                if (stack.isEmpty() || !input.ingredient().test(stack)) continue;

                var shrinkCount = Math.min(input.count() - inputRemoved, stack.getCount());
                stack.shrink(shrinkCount);
                inputRemoved += shrinkCount;

                if (inputRemoved >= input.count()) break;
            }

            actualRemoved += inputRemoved;
        }

        if (actualRemoved < toRemove) {
            inventory.clear();
            for (int i = 0; i < itemBackup.size(); i++) {
                inventory.add(i, itemBackup.get(i));
            }
            return false;
        }

        catalyst = ItemStack.EMPTY;
        rebuildInsertOrder();
        onContentsChanged();
        return true;
    }

    private ItemStack insertItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        validateSlot(slot);

        var currentStack = inventory.get(slot);
        if (currentStack.isEmpty()) {
            inventory.set(slot, stack);
            onContentsChanged();
            return ItemStack.EMPTY;
        }

        if (!ItemStack.isSameItem(currentStack, stack)) {
            return stack;
        }

        var maxCount = getMaxStackSize(slot, currentStack);
        var toInsert = Math.min(maxCount - currentStack.getCount(), stack.getCount());
        if (toInsert <= 0) return stack;

        currentStack.grow(toInsert);
        var remainder = stack.copyWithCount(stack.getCount() - toInsert);
        onContentsChanged();

        return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
    }

    private void rebuildInsertOrder() {
        insertOrder.clear();
        for (var i = SIZE - 1; i >= 0; i--) {
            var stack = inventory.get(i);
            if (stack.isEmpty()) continue;
            insertOrder.add(new Tuple<>(stack.copy(), i));
        }
    }

    private void onContentsChanged() {
        altar.setChanged();
        if (altar.getLevel() == null || altar.getLevel().isClientSide) return;
        altar.getLevel().sendBlockUpdated(altar.getBlockPos(), altar.getBlockState(), altar.getBlockState(), 1 | 2);
    }

    private int getMaxStackSize(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public int getSlots() {
        return AltarRecipeSerializer.MAX_INPUTS + 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlot(slot);
        if (slot == AltarRecipeSerializer.MAX_INPUTS) return catalyst;
        return inventory.get(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (simulate) return ItemStack.EMPTY;
        return altar.handleInteraction(null, stack);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlot(slot);
        if (slot == AltarRecipeSerializer.MAX_INPUTS) return 1;
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    public List<ItemStack> getNoneEmptyItems() {
        return inventory.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
    }

    public ItemStack getCatalyst() {
        return catalyst;
    }

    public void setCatalyst(ItemStack catalyst) {
        this.catalyst = catalyst;
        onContentsChanged();
    }

    private List<ItemStack> createItemBackup() {
        List<ItemStack> backup = new ArrayList<>();
        for (var stack : inventory) {
            if (stack.isEmpty()) continue;
            backup.add(stack.copy());
        }
        return backup;
    }

    @Override
    public ItemStack getItem(int index) {
        return getStackInSlot(index);
    }

    @Override
    public int size() {
        return getSlots();
    }

    private static final class InternalInventory implements INBTSerializable<ListTag> {

        private ItemStack[] items;

        private InternalInventory(int size) {
            this.items = new ItemStack[size];
            clear();
        }

        private void set(int slot, ItemStack stack) {
            items[slot] = stack;
        }

        private ItemStack get(int slot) {
            return items[slot];
        }

        private void remove(int slot) {
            items[slot] = ItemStack.EMPTY;
        }

        private void clear() {
            Arrays.fill(items, ItemStack.EMPTY);
        }

        private int size() {
            return items.length;
        }

        @Override
        @UnknownNullability
        public ListTag serializeNBT(HolderLookup.Provider provider) {
            ListTag itemsListTag = new ListTag();

            for (int i = 0; i < items.length; i++) {
                if (items[i].isEmpty()) continue;
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt(Constants.SLOT, i);
                Tag finishedItemTag = items[i].save(provider, itemTag);
                itemsListTag.add(finishedItemTag);
            }

            return itemsListTag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, ListTag listTag) {
            items = new ItemStack[listTag.size()];

            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                int slot = itemTag.getInt(Constants.SLOT);
                ItemStack.parse(provider, itemTag).ifPresent(stack -> items[slot] = stack);
            }
        }
    }
}

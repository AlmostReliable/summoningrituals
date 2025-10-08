package com.almostreliable.summoningrituals.altar.inventory;

import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class AltarInventory implements IItemHandlerModifiable, RecipeInput, INBTSerializable<CompoundTag> {

    private final AltarInventoryHost host;
    private final InternalInventory inventory;
    private final Stack<Tuple<ItemStack, Integer>> insertOrder;

    private ItemStack catalyst;

    public AltarInventory(AltarInventoryHost host) {
        this.host = host;
        int size = Config.COMMON.altarInventorySize.get();
        this.inventory = new InternalInventory(size);
        this.insertOrder = new Stack<>();
        this.catalyst = ItemStack.EMPTY;
    }

    @Override
    @UnknownNullability
    public CompoundTag serializeNBT(HolderLookup.Provider registryAccess) {
        var compoundTag = serializeWithoutInsertOrder(registryAccess);

        var insertOrderTag = new ListTag();
        for (var entry : insertOrder) {
            var entryTag = new CompoundTag();
            entryTag.putInt(Constants.SLOT, entry.getB());
            var finishedEntryTag = entry.getA().save(registryAccess, entryTag);
            insertOrderTag.add(finishedEntryTag);
        }
        compoundTag.put(Constants.INSERT_ORDER, insertOrderTag);

        return compoundTag;
    }

    public CompoundTag serializeWithoutInsertOrder(HolderLookup.Provider registryAccess) {
        var inventoryTag = inventory.serializeNBT(registryAccess);

        var compoundTag = new CompoundTag();
        compoundTag.put(Constants.INVENTORY, inventoryTag);
        compoundTag.put(Constants.CATALYST, catalyst.saveOptional(registryAccess));

        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag tag) {
        var inventoryTag = tag.getCompound(Constants.INVENTORY);
        inventory.deserializeNBT(registryAccess, inventoryTag);

        if (tag.contains(Constants.INSERT_ORDER)) {
            var insertOrderTag = tag.getList(Constants.INSERT_ORDER, Tag.TAG_COMPOUND);
            insertOrder.clear();
            for (var i = 0; i < insertOrderTag.size(); i++) {
                var entryTag = insertOrderTag.getCompound(i);
                var slot = entryTag.getInt(Constants.SLOT);
                ItemStack.parse(registryAccess, entryTag).ifPresent(stack -> insertOrder.add(new Tuple<>(stack, slot)));
            }
        }

        var catalystTag = tag.getCompound(Constants.CATALYST);
        catalyst = ItemStack.parseOptional(registryAccess, catalystTag);
    }

    @Override
    public int getSlots() {
        return inventory.slots() + 1;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot == getCatalystSlot()) return 1;
        return Item.DEFAULT_MAX_STACK_SIZE;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot == getCatalystSlot()) return catalyst;
        return inventory.get(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot == getCatalystSlot()) {
            Preconditions.checkArgument(stack.getCount() == 1, "catalyst must be a single item");
            catalyst = stack;
        } else {
            inventory.set(slot, stack);
        }

        onContentsChanged();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (slot == getCatalystSlot()) {
            return AltarRecipe.isCatalyst(stack.getItem());
        }

        return AltarRecipe.isInput(stack.getItem());
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        return host.handleItemInsertion(null, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    // region RecipeInput delegate
    @Override
    public ItemStack getItem(int index) {
        return getStackInSlot(index);
    }

    @Override
    public int size() {
        return getSlots();
    }
    // endregion RecipeInput delegate

    /**
     * Inserts an input {@link ItemStack} into the inventory while tracking the insertion order.
     * The method iterates the inventory and tries to insert or merge the stack into each slot
     * until the stack is fully inserted or the inventory is full.
     *
     * @param inputStack The input {@link ItemStack} to insert into the inventory.
     * @return The remaining {@link ItemStack} if the inventory cannot fully accommodate the input stack.
     */
    public ItemStack insertInputTracked(ItemStack inputStack) {
        var remainingCount = inputStack.getCount();

        for (var slot = 0; slot < inventory.slots(); slot++) {
            var stackToInsert = inputStack.copyWithCount(remainingCount);
            remainingCount = insertOrFillItemInSlot(slot, stackToInsert);

            if (remainingCount == 0) {
                trackInsert(slot, stackToInsert.copy());
                return ItemStack.EMPTY;
            }

            if (remainingCount == stackToInsert.getCount()) {
                continue;
            }

            var insertedCount = stackToInsert.getCount() - remainingCount;
            trackInsert(slot, stackToInsert.copyWithCount(insertedCount));
        }

        if (remainingCount == 0) {
            return ItemStack.EMPTY;
        }

        inputStack.setCount(remainingCount);
        return inputStack;
    }

    /**
     * Attempts to insert the given {@link ItemStack} into a specified slot in the inventory.
     * If the slot is empty, the stack is inserted directly. If the slot already contains items,
     * it tries to merge the stack with the existing stack, respecting stack size limits.
     *
     * @param slot  The index of the inventory slot to insert into.
     * @param stack The {@link ItemStack} to be inserted or merged into the slot.
     * @return The remaining item count that could not be inserted into the slot.
     */
    private int insertOrFillItemInSlot(int slot, ItemStack stack) {
        var stackInSlot = inventory.get(slot);

        if (stackInSlot.isEmpty()) {
            setStackInSlot(slot, stack);
            return 0;
        }

        if (!ItemStack.isSameItemSameComponents(stackInSlot, stack)) {
            return stack.getCount();
        }

        var maxCount = Math.min(getSlotLimit(slot), stackInSlot.getMaxStackSize());
        var toInsert = Math.min(maxCount - stackInSlot.getCount(), stack.getCount());
        if (toInsert <= 0) return stack.getCount();

        stackInSlot.grow(toInsert);
        onContentsChanged();

        return stack.getCount() - toInsert;
    }

    private void trackInsert(int slot, ItemStack stack) {
        insertOrder.push(new Tuple<>(stack, slot));
    }

    public void removeLastInsertedItem() {
        if (!catalyst.isEmpty()) {
            host.spawnItemAboveAltar(catalyst);
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
            inventory.remove(slot);
        }
        onContentsChanged();

        host.spawnItemAboveAltar(stack);
    }

    public boolean handleRecipe(AltarRecipe recipe) {
        var itemBackup = createItemBackup();

        var toRemove = 0;
        var actualRemoved = 0;

        for (var input : recipe.itemInputs()) {
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
            for (var i = 0; i < itemBackup.size(); i++) {
                inventory.set(i, itemBackup.get(i));
            }
            return false;
        }

        catalyst = ItemStack.EMPTY;
        rebuildInsertOrder();
        onContentsChanged();
        return true;
    }

    private void onContentsChanged() {
        host.onInventoryChanged(this::serializeWithoutInsertOrder);
    }

    private List<ItemStack> createItemBackup() {
        var backup = new ArrayList<ItemStack>();
        for (var i = 0; i < inventory.slots(); i++) {
            var stack = inventory.get(i);
            if (!stack.isEmpty()) backup.add(stack.copy());
        }
        return backup;
    }

    private void rebuildInsertOrder() {
        insertOrder.clear();
        for (var i = inventory.slots(); i >= 0; i--) {
            var stack = inventory.get(i);
            if (stack.isEmpty()) continue;
            insertOrder.add(new Tuple<>(stack.copy(), i));
        }
    }

    public List<ItemStack> getNoneEmptyItems() {
        var items = new ArrayList<ItemStack>();
        for (var stack : inventory) {
            if (!stack.isEmpty()) items.add(stack);
        }
        return items;
    }

    private int getCatalystSlot() {
        return inventory.slots();
    }

    public ItemStack getCatalyst() {
        return catalyst;
    }

    public void setCatalyst(ItemStack catalyst) {
        this.catalyst = catalyst;
        onContentsChanged();
    }
}

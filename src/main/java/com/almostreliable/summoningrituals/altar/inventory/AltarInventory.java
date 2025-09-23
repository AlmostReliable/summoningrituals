package com.almostreliable.summoningrituals.altar.inventory;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.network.AltarInventorySyncPacket;
import com.almostreliable.summoningrituals.network.PacketHandler;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.util.GameUtils;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

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
    public CompoundTag serializeNBT(HolderLookup.Provider registryAccess) {
        CompoundTag compoundTag = serializeWithoutInsertOrder(registryAccess);

        ListTag insertOrderTag = new ListTag();
        for (var entry : insertOrder) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt(Constants.SLOT, entry.getB());
            Tag finishedEntryTag = entry.getA().save(registryAccess, entryTag);
            insertOrderTag.add(finishedEntryTag);
        }
        compoundTag.put(Constants.INSERT_ORDER, insertOrderTag);

        return compoundTag;
    }

    public CompoundTag serializeWithoutInsertOrder(HolderLookup.Provider registryAccess) {
        ListTag inventoryTag = inventory.serializeNBT(registryAccess);

        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put(Constants.INVENTORY, inventoryTag);
        compoundTag.put(Constants.CATALYST, catalyst.saveOptional(registryAccess));

        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag tag) {
        ListTag inventoryTag = tag.getList(Constants.INVENTORY, Tag.TAG_COMPOUND);
        inventory.deserializeNBT(registryAccess, inventoryTag);

        if (tag.contains(Constants.INSERT_ORDER)) {
            ListTag insertOrderTag = tag.getList(Constants.INSERT_ORDER, Tag.TAG_COMPOUND);
            insertOrder.clear();
            for (int i = 0; i < insertOrderTag.size(); i++) {
                CompoundTag entryTag = insertOrderTag.getCompound(i);
                int slot = entryTag.getInt(Constants.SLOT);
                ItemStack.parse(registryAccess, entryTag).ifPresent(stack -> insertOrder.add(new Tuple<>(stack, slot)));
            }
        }

        CompoundTag catalystTag = tag.getCompound(Constants.CATALYST);
        catalyst = ItemStack.parseOptional(registryAccess, catalystTag);
    }

    @Override
    public int getSlots() {
        return inventory.size() + 1;
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
        return true;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return altar.handleInteraction(null, stack, simulate);
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

    private int getCatalystSlot() {
        return inventory.size();
    }

    public ItemStack handleInsertion(ItemStack stack) {
        var remaining = stack.copy();
        for (var i = 0; i < inventory.size(); i++) {
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
            inventory.remove(slot);
        }
        onContentsChanged();

        GameUtils.dropItem(level, altar.getBlockPos(), stack, true);
    }

    public boolean handleRecipe(AltarRecipe recipe) {
        var itemBackup = createItemBackup();

        var toRemove = 0;
        var actualRemoved = 0;

        for (var input : recipe.inputs()) {
            toRemove += input.count();
            var inputRemoved = 0;

            for (ItemStack stack : inventory) {
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
                inventory.set(i, itemBackup.get(i));
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

        var currentStack = inventory.get(slot);
        if (currentStack.isEmpty()) {
            inventory.set(slot, stack);
            onContentsChanged();
            return ItemStack.EMPTY;
        }

        if (!ItemStack.isSameItem(currentStack, stack)) {
            return stack;
        }

        var maxCount = Math.min(getSlotLimit(slot), currentStack.getMaxStackSize());
        var toInsert = Math.min(maxCount - currentStack.getCount(), stack.getCount());
        if (toInsert <= 0) return stack;

        currentStack.grow(toInsert);
        var remainder = stack.copyWithCount(stack.getCount() - toInsert);
        onContentsChanged();

        return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
    }

    private void rebuildInsertOrder() {
        insertOrder.clear();
        for (var i = inventory.size(); i >= 0; i--) {
            var stack = inventory.get(i);
            if (stack.isEmpty()) continue;
            insertOrder.add(new Tuple<>(stack.copy(), i));
        }
    }

    private void onContentsChanged() {
        if (!(altar.getLevel() instanceof ServerLevel level)) return;

        altar.setChanged();
        PacketHandler.sendToTrackingChunk(
            level,
            altar.getBlockPos(),
            new AltarInventorySyncPacket(altar.getBlockPos(), serializeWithoutInsertOrder(level.registryAccess()))
        );
    }

    public List<ItemStack> getNoneEmptyItems() {
        var items = new ArrayList<ItemStack>();
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) items.add(stack);
        }
        return items;
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
        for (var i = 0; i < inventory.size(); i++) {
            var stack = inventory.get(i);
            if (!stack.isEmpty()) backup.add(stack.copy());
        }
        return backup;
    }
}

package com.almostreliable.summoningrituals.inventory;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.altar.AltarEntity;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.util.GameUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class AltarInventory implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {

    private final AltarEntity parent;
    private final AltarInvWrapper vanillaInv;
    private final Deque<Tuple<ItemStack, Integer>> insertOrder;
    private List<ItemStack> items;
    private ItemStack catalyst;

    public AltarInventory(AltarEntity parent) {
        this.parent = parent;
        vanillaInv = new AltarInvWrapper(this);
        items = new ArrayList<>();
        catalyst = ItemStack.EMPTY;
        insertOrder = new ArrayDeque<>();
    }

    @Override
    public CompoundTag serializeNBT() {
        var insertList = new ListTag();
        for (var insert : insertOrder) {
            var insertTag = new CompoundTag();
            insert.getA().save(insertTag);
            insertTag.putInt(Constants.SLOT, insert.getB());
            insertList.add(insertTag);
        }
        var itemList = new ListTag();
        for (var slot = 0; slot < items.size(); slot++) {
            if (!items.get(slot).isEmpty()) {
                var itemTag = new CompoundTag();
                itemTag.putInt(Constants.SLOT, slot);
                items.get(slot).save(itemTag);
                itemList.add(itemTag);
            }
        }
        var tag = new CompoundTag();
        tag.put(Constants.INSERT_ORDER, insertList);
        tag.putInt(Constants.SIZE, items.size());
        tag.put(Constants.ITEMS, itemList);
        tag.put(Constants.CATALYST, catalyst.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        var insertList = tag.getList(Constants.INSERT_ORDER, Tag.TAG_COMPOUND);
        insertOrder.clear();
        for (var insertTag : insertList) {
            var stack = ItemStack.of((CompoundTag) insertTag);
            insertOrder.add(new Tuple<>(stack, ((CompoundTag) insertTag).getInt(Constants.SLOT)));
        }
        items = new ArrayList<>();
        for (var i = 0; i < tag.getInt(Constants.SIZE); i++) {
            items.add(ItemStack.EMPTY);
        }
        var itemList = tag.getList(Constants.ITEMS, Tag.TAG_COMPOUND);
        for (var itemTag : itemList) {
            var slot = ((CompoundTag) itemTag).getInt(Constants.SLOT);
            if (slot >= 0 && slot < items.size()) {
                items.set(slot, ItemStack.of((CompoundTag) itemTag));
            }
        }
        catalyst = ItemStack.of(tag.getCompound(Constants.CATALYST));
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        validateSlot(slot);
        if (slot == items.size()) {
            setCatalyst(stack);
            return;
        }
        items.set(slot, stack);
        onContentsChanged();
    }

    public ItemStack handleInsertion(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        var remaining = stack;
        for (var i = 0; i < items.size(); i++) {
            remaining = insertItem(i, remaining);
            if (remaining.isEmpty()) {
                insertOrder.push(new Tuple<>(stack, i));
                return ItemStack.EMPTY;
            }
            if (remaining.getCount() == stack.getCount()) {
                continue;
            }
            stack.shrink(remaining.getCount());
            insertOrder.push(new Tuple<>(stack.copy(), i));
            return remaining;
        }

        items.add(stack);
        insertOrder.push(new Tuple<>(stack.copy(), items.size() - 1));
        onContentsChanged();
        return ItemStack.EMPTY;
    }

    public void popLastInserted() {
        assert parent.getLevel() != null && !parent.getLevel().isClientSide;

        if (!catalyst.isEmpty()) {
            GameUtils.dropItem(parent.getLevel(), parent.getBlockPos(), catalyst, true);
            catalyst = ItemStack.EMPTY;
            onContentsChanged();
            return;
        }

        if (insertOrder.isEmpty()) return;
        var last = insertOrder.pop();
        var stack = last.getA();
        int slot = last.getB();
        items.get(slot).shrink(stack.getCount());
        if (items.get(slot).isEmpty()) {
            trimInventory();
        }
        onContentsChanged();
        GameUtils.dropItem(parent.getLevel(), parent.getBlockPos(), stack, true);
    }

    public void dropContents() {
        var level = parent.getLevel();
        assert level != null && !level.isClientSide;
        var pos = parent.getBlockPos();
        for (var stack : items) {
            if (stack.isEmpty()) continue;
            GameUtils.dropItem(level, pos, stack, false);
        }
        if (!catalyst.isEmpty()) {
            GameUtils.dropItem(level, pos, catalyst, false);
        }
    }

    public boolean handleRecipe(AltarRecipe recipe) {
        var oldItems = new ArrayList<>(items);
        var toRemove = 0;
        var removed = 0;
        for (var input : recipe.getInputs()) {
            toRemove += input.count();
            var inputRemoved = 0;
            for (var stack : items) {
                if (stack.isEmpty() || !input.ingredient().test(stack)) continue;
                var shrinkCount = Math.min(input.count() - inputRemoved, stack.getCount());
                stack.shrink(shrinkCount);
                inputRemoved += shrinkCount;
                if (inputRemoved >= input.count()) break;
            }
            removed += inputRemoved;
        }
        if (removed < toRemove) {
            items = oldItems;
            return false;
        }
        catalyst = ItemStack.EMPTY;
        rebuildInsertOrder();
        onContentsChanged();
        return true;
    }

    private ItemStack insertItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!isItemValid(slot, stack)) return stack;
        validateSlot(slot);

        var current = items.get(slot);
        var limit = getStackLimit(slot, stack);
        if (!current.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, current)) return stack;
            limit -= current.getCount();
        }
        if (limit <= 0) return stack;

        var reachedLimit = stack.getCount() > limit;
        if (current.isEmpty()) {
            items.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
        } else {
            current.grow(reachedLimit ? limit : stack.getCount());
        }
        onContentsChanged();

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    private void rebuildInsertOrder() {
        insertOrder.clear();
        trimInventory();
        for (var i = items.size() - 1; i >= 0; i--) {
            insertOrder.add(new Tuple<>(items.get(i).copy(), i));
        }
    }

    private void trimInventory() {
        for (var i = items.size() - 1; i >= 0; i--) {
            if (items.get(i).isEmpty()) items.remove(i);
        }
    }

    private void onContentsChanged() {
        parent.setChanged();
        if (parent.getLevel() == null || parent.getLevel().isClientSide) return;
        parent.getLevel().sendBlockUpdated(parent.getBlockPos(), parent.getBlockState(), parent.getBlockState(), 1 | 2);
    }

    private void validateSlot(int slot) {
        if (slot < 0 || slot >= items.size() + 1) {
            throw new IllegalStateException(f("Slot {} is not in range [0,{})", slot, items.size()));
        }
    }

    private int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public int getSlots() {
        return items.size() + 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlot(slot);
        if (slot == items.size()) {
            return catalyst;
        }
        return items.get(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (simulate) return ItemStack.EMPTY;
        return parent.handleInteraction(null, stack);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        validateSlot(slot);

        if (slot == items.size()) return ItemStack.EMPTY;
        var current = items.get(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        var toExtract = Math.min(amount, current.getMaxStackSize());

        if (current.getCount() <= toExtract) {
            if (!simulate) {
                items.set(slot, ItemStack.EMPTY);
                onContentsChanged();
                return current;
            }
            return current.copy();
        }
        if (!simulate) {
            items.set(slot, ItemHandlerHelper.copyStackWithSize(current, current.getCount() - toExtract));
            onContentsChanged();
        }

        return ItemHandlerHelper.copyStackWithSize(current, toExtract);
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlot(slot);
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }

    public AltarInvWrapper getVanillaInv() {
        return vanillaInv;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public ItemStack getCatalyst() {
        return catalyst;
    }

    public void setCatalyst(ItemStack catalyst) {
        this.catalyst = catalyst;
        onContentsChanged();
    }
}

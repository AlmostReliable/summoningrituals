package com.almostreliable.summoningrituals.inventory;

import com.almostreliable.summoningrituals.altar.AltarEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import static com.almostreliable.summoningrituals.Utils.f;

public class AltarInventory implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {

    private static final String SLOT = "slot";
    private static final String SIZE = "size";
    private static final String ITEMS = "items";

    private final AltarEntity parent;
    private final AltarInvWrapper vanillaInv;
    private NonNullList<ItemStack> stacks;

    public AltarInventory(AltarEntity parent) {
        this.parent = parent;
        stacks = NonNullList.create();
        vanillaInv = new AltarInvWrapper(this, parent);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tagList = new ListTag();
        for (var slot = 0; slot < stacks.size(); slot++) {
            if (!stacks.get(slot).isEmpty()) {
                var itemTag = new CompoundTag();
                itemTag.putInt(SLOT, slot);
                stacks.get(slot).save(itemTag);
                tagList.add(itemTag);
            }
        }
        var tag = new CompoundTag();
        tag.putInt(SIZE, stacks.size());
        tag.put(ITEMS, tagList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        setSize(tag.contains(SIZE) ? tag.getInt(SIZE) : stacks.size());
        var tagList = tag.getList(ITEMS, Tag.TAG_COMPOUND);
        for (var i = 0; i < tagList.size(); i++) {
            var itemTags = tagList.getCompound(i);
            var slot = itemTags.getInt(SLOT);
            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, ItemStack.of(itemTags));
            }
        }
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        validateSlot(slot);
        stacks.set(slot, stack);
        onContentsChanged();
    }

    private void onContentsChanged() {
        parent.setChanged();
    }

    private void validateSlot(int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            throw new IllegalStateException(f("Slot {} is not in range [0,{})", slot, stacks.size()));
        }
    }

    private int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlot(slot);
        return stacks.get(slot);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!isItemValid(slot, stack)) return stack;
        validateSlot(slot);

        var current = stacks.get(slot);
        var limit = getStackLimit(slot, stack);
        if (!current.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, current)) return stack;
            limit -= current.getCount();
        }
        if (limit <= 0) return stack;

        var reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (current.isEmpty()) {
                stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                current.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged();
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        validateSlot(slot);

        var current = stacks.get(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        var toExtract = Math.min(amount, current.getMaxStackSize());

        if (current.getCount() <= toExtract) {
            if (!simulate) {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged();
                return current;
            }
            return current.copy();
        }
        if (!simulate) {
            stacks.set(slot, ItemHandlerHelper.copyStackWithSize(current, current.getCount() - toExtract));
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
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }

    private void setSize(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }
}

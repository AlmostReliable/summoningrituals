package com.almostreliable.summoningrituals.inventory;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.platform.PlatformBlockEntity;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.AltarRecipeSerializer;
import com.almostreliable.summoningrituals.util.GameUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class AltarInventory implements ItemHandler, RecipeInput {

    public static final int SIZE = 64;

    private final PlatformBlockEntity parent;
    private final VanillaWrapper vanillaInv;
    private final Deque<Tuple<ItemStack, Integer>> insertOrder;
    private final NonNullList<ItemStack> items;

    private ItemStack catalyst;

    public AltarInventory(PlatformBlockEntity parent) {
        this.parent = parent;
        vanillaInv = new VanillaWrapper(this);
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        catalyst = ItemStack.EMPTY;
        insertOrder = new ArrayDeque<>(SIZE);
    }

    @Override
    public CompoundTag serialize() {
        var insertListTag = new ListTag();
        for (var e : insertOrder) {
            var tag = new CompoundTag();
            e.getA().save(tag);
            tag.putInt(Constants.SLOT, e.getB());
            insertListTag.add(tag);
        }

        var itemsTag = new ListTag();
        for (var slot = 0; slot < SIZE; slot++) {
            if (items.get(slot).isEmpty()) continue;
            var tag = new CompoundTag();
            tag.putInt(Constants.SLOT, slot);
            items.get(slot).save(tag);
            itemsTag.add(tag);
        }

        var tag = new CompoundTag();
        tag.put(Constants.INSERT_ORDER, insertListTag);
        tag.put(Constants.ITEMS, itemsTag);
        tag.put(Constants.CATALYST, catalyst.serialize());
        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        insertOrder.clear();
        var insertListTag = tag.getList(Constants.INSERT_ORDER, Tag.TAG_COMPOUND);
        for (var e : insertListTag) {
            var stack = ItemStack.of((CompoundTag) e);
            var slot = ((CompoundTag) e).getInt(Constants.SLOT);
            insertOrder.add(new Tuple<>(stack, slot));
        }

        items.clear();
        var itemsTag = tag.getList(Constants.ITEMS, Tag.TAG_COMPOUND);
        for (var e : itemsTag) {
            var slot = ((CompoundTag) e).getInt(Constants.SLOT);
            var stack = ItemStack.of((CompoundTag) e);
            items.set(slot, stack);
        }

        catalyst = ItemStack.of(tag.getCompound(Constants.CATALYST));
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlot(slot);

        if (slot == AltarRecipeSerializer.MAX_INPUTS) {
            catalyst = stack;
            return;
        }

        items.set(slot, stack);
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
        var level = parent.getLevel();
        assert level != null && !level.isClientSide;

        if (!catalyst.isEmpty()) {
            GameUtils.dropItem(level, parent.getBlockPos(), catalyst, true);
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
            items.set(slot, ItemStack.EMPTY);
        }
        onContentsChanged();

        GameUtils.dropItem(level, parent.getBlockPos(), stack, true);
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
        var itemBackup = createItemBackup();

        var toRemove = 0;
        var actualRemoved = 0;

        for (var input : recipe.inputs()) {
            toRemove += input.count();
            var inputRemoved = 0;

            for (var stack : items) {
                if (stack.isEmpty() || !input.ingredient().test(stack)) continue;

                var shrinkCount = Math.min(input.count() - inputRemoved, stack.getCount());
                stack.shrink(shrinkCount);
                inputRemoved += shrinkCount;

                if (inputRemoved >= input.count()) break;
            }

            actualRemoved += inputRemoved;
        }

        if (actualRemoved < toRemove) {
            items.clear();
            for (int i = 0; i < itemBackup.size(); i++) {
                items.add(i, itemBackup.get(i));
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

        var currentStack = items.get(slot);
        if (currentStack.isEmpty()) {
            items.set(slot, stack);
            onContentsChanged();
            return ItemStack.EMPTY;
        }

        if (!currentStack.canStack(stack)) return stack;

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
            var stack = items.get(i);
            if (stack.isEmpty()) continue;
            insertOrder.add(new Tuple<>(stack.copy(), i));
        }
    }

    private void onContentsChanged() {
        parent.setChanged();
        if (parent.getLevel() == null || parent.getLevel().isClientSide) return;
        parent.getLevel().sendBlockUpdated(parent.getBlockPos(), parent.getBlockState(), parent.getBlockState(), 1 | 2);
    }

    private void validateSlot(int slot) {
        if (slot < 0 || slot >= SIZE + 1) {
            throw new IllegalStateException(f("Slot {} is not in range [0,{})", slot, SIZE + 1));
        }
    }

    private int getMaxStackSize(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public int getSlots() {
        return AltarRecipeSerializer.MAX_INPUTS + 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlot(slot);
        if (slot == AltarRecipeSerializer.MAX_INPUTS) return catalyst;
        return items.get(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (simulate) return ItemStack.EMPTY;
        return parent.handleInteraction(null, stack);
    }

    @Nonnull
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

    @Override
    public List<ItemStack> getNoneEmptyItems() {
        return items.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public ItemStack getCatalyst() {
        return catalyst;
    }

    public void setCatalyst(ItemStack catalyst) {
        this.catalyst = catalyst;
        onContentsChanged();
    }

    public VanillaWrapper getVanillaInv() {
        return vanillaInv;
    }

    private List<ItemStack> createItemBackup() {
        List<ItemStack> backup = new ArrayList<>();
        for (var stack : items) {
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
}

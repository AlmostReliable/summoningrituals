package com.almostreliable.summoningrituals.inventory;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.platform.PlatformBlockEntity;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.util.GameUtils;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class AltarInventory implements ItemHandler {

    public static final int SIZE = 64;

    private final PlatformBlockEntity parent;
    @val final VanillaWrapper vanillaInv;
    @val final StorageWrapper storageInv;
    private final Deque<Tuple<ItemStack, Integer>> insertOrder;
    private NonNullList<ItemStack> items;
    @override
    @var ItemStack catalyst;

    public AltarInventory(PlatformBlockEntity parent) {
        this.parent = parent;
        vanillaInv = new VanillaWrapper(this);
        storageInv = new StorageWrapper(this);
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        catalyst = ItemStack.EMPTY;
        insertOrder = new ArrayDeque<>(SIZE);
    }

    @Override
    public CompoundTag serialize() {
        var insertListTag = new ListTag();
        for (var e : insertOrder) {
            var tag = new CompoundTag();
            e.a.save(tag);
            tag.putInt(Constants.SLOT, e.b);
            insertListTag.add(tag);
        }

        var itemsTag = new ListTag();
        for (var slot = 0; slot < SIZE; slot++) {
            if (items.get(slot).isEmpty) continue;
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

        if (slot == SIZE) {
            catalyst = stack;
            return;
        }

        items.set(slot, stack);
        onContentsChanged();
    }

    public ItemStack handleInsertion(ItemStack stack) {
        if (stack.isEmpty) return ItemStack.EMPTY;

        var remaining = stack.copy();
        for (var i = 0; i < SIZE; i++) {
            remaining = insertItem(i, remaining);

            if (remaining.isEmpty) {
                insertOrder.push(new Tuple<>(stack, i));
                return ItemStack.EMPTY;
            }

            if (remaining.count == stack.count) {
                continue;
            }

            stack.shrink(remaining.count);
            insertOrder.push(new Tuple<>(stack.copy(), i));
        }

        return stack;
    }

    public void popLastInserted() {
        var level = parent.level;
        assert level != null && !level.isClientSide;

        if (!catalyst.isEmpty) {
            GameUtils.dropItem(level, parent.blockPos, catalyst, true);
            catalyst = ItemStack.EMPTY;
            onContentsChanged();
            return;
        }

        if (insertOrder.isEmpty) return;

        var last = insertOrder.pop();
        var stack = last.a;
        int slot = last.b;

        items.get(slot).shrink(stack.count);
        if (items.get(slot).isEmpty) {
            items.set(slot, ItemStack.EMPTY);
        }
        onContentsChanged();

        GameUtils.dropItem(level, parent.blockPos, stack, true);
    }

    public void dropContents() {
        var level = parent.level;
        assert level != null && !level.isClientSide;

        var pos = parent.blockPos;
        for (var stack : items) {
            if (stack.isEmpty) continue;
            GameUtils.dropItem(level, pos, stack, false);
        }

        if (!catalyst.isEmpty) {
            GameUtils.dropItem(level, pos, catalyst, false);
        }
    }

    public boolean handleRecipe(AltarRecipe recipe) {
        var itemBackup = createItemBackup();

        var toRemove = 0;
        var actualRemoved = 0;

        for (var input : recipe.inputs) {
            toRemove += input.count();
            var inputRemoved = 0;

            for (var stack : items) {
                if (stack.isEmpty || !input.ingredient().test(stack)) continue;

                var shrinkCount = Math.min(input.count() - inputRemoved, stack.count);
                stack.shrink(shrinkCount);
                inputRemoved += shrinkCount;

                if (inputRemoved >= input.count()) break;
            }

            actualRemoved += inputRemoved;
        }

        if (actualRemoved < toRemove) {
            items = NonNullList.of(ItemStack.EMPTY, itemBackup.toArray(new ItemStack[0]));
            return false;
        }

        catalyst = ItemStack.EMPTY;
        rebuildInsertOrder();
        onContentsChanged();
        return true;
    }

    private ItemStack insertItem(int slot, ItemStack stack) {
        if (stack.isEmpty) return ItemStack.EMPTY;
        validateSlot(slot);

        var currentStack = items.get(slot);
        if (currentStack.isEmpty) {
            items.set(slot, stack);
            onContentsChanged();
            return ItemStack.EMPTY;
        }

        if (!currentStack.canStack(stack)) return stack;

        var maxCount = getMaxStackSize(slot, currentStack);
        var toInsert = Math.min(maxCount - currentStack.count, stack.count);
        if (toInsert <= 0) return stack;

        currentStack.grow(toInsert);
        stack.shrink(toInsert);
        onContentsChanged();

        return stack.isEmpty ? ItemStack.EMPTY : stack;
    }

    private void rebuildInsertOrder() {
        insertOrder.clear();
        for (var i = SIZE - 1; i >= 0; i--) {
            var stack = items.get(i);
            if (stack.isEmpty) continue;
            insertOrder.add(new Tuple<>(stack.copy(), i));
        }
    }

    private void onContentsChanged() {
        parent.setChanged();
        if (parent.level == null || parent.level.isClientSide) return;
        parent.level.sendBlockUpdated(parent.blockPos, parent.blockState, parent.blockState, 1 | 2);
    }

    private void validateSlot(int slot) {
        if (slot < 0 || slot >= SIZE + 1) {
            throw new IllegalStateException(f("Slot {} is not in range [0,{})", slot, SIZE + 1));
        }
    }

    private int getMaxStackSize(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.maxStackSize);
    }

    @Override
    public int getSlots() {
        return SIZE + 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlot(slot);
        if (slot == items.size()) return catalyst;
        return items.get(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (simulate) return ItemStack.EMPTY;
        return parent.handleInteraction(null, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlot(slot);
        if (slot == SIZE) return 1;
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public List<ItemStack> getNoneEmptyItems() {
        return items.stream().filter(stack -> !stack.isEmpty).collect(Collectors.toList());
    }

    public void setCatalyst(ItemStack catalyst) {
        this.catalyst = catalyst;
        onContentsChanged();
    }

    private List<ItemStack> createItemBackup() {
        return items.stream().filter(stack -> !stack.isEmpty).map(ItemStack::copy).collect(Collectors.toList());
    }

    @SuppressWarnings("UnstableApiUsage")
    public final class StorageWrapper extends SnapshotParticipant<StorageWrapper.InventorySnapshot> implements InsertionOnlyStorage<ItemVariant> {

        private final AltarInventory delegate;

        private StorageWrapper(AltarInventory delegate) {
            this.delegate = delegate;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            var stack = resource.toStack();
            var count = Math.min(stack.getMaxStackSize(), maxAmount);
            updateSnapshots(transaction);
            var remaining = delegate.insertItem(-1, stack.withCount(count), false);
            return count - remaining.getCount();
        }

        @Override
        protected InventorySnapshot createSnapshot() {
            return new InventorySnapshot(delegate);
        }

        @Override
        protected void readSnapshot(InventorySnapshot snapshot) {
            items = NonNullList.of(ItemStack.EMPTY, snapshot.itemBackup.toArray(new ItemStack[0]));
            delegate.catalyst = snapshot.catalystBackup;
            delegate.rebuildInsertOrder();
        }

        @Override
        protected void onFinalCommit() {
            delegate.onContentsChanged();
        }

        private static final class InventorySnapshot {

            private final List<ItemStack> itemBackup;
            private final ItemStack catalystBackup;

            private InventorySnapshot(AltarInventory delegate) {
                this.itemBackup = delegate.createItemBackup();
                this.catalystBackup = delegate.catalyst.copy();
            }
        }
    }
}

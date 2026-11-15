package com.almostreliable.summoningrituals.altar.inventory;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import com.google.common.base.Preconditions;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class AltarInventory implements ResourceHandler<ItemResource>, RecipeInput {

    private final AltarInventoryHost host;
    private final InternalInventory inventory;
    private final Stack<Tuple<ItemStack, Integer>> insertOrder;
    private final PendingInsertion pendingInsertion;

    private ItemStack initiator;
    private boolean suppressChanges;

    public AltarInventory(AltarInventoryHost host) {
        this.host = host;
        this.inventory = new InternalInventory(Config.COMMON.inventorySize.get());
        this.insertOrder = new Stack<>();
        this.pendingInsertion = new PendingInsertion();
        this.initiator = ItemStack.EMPTY;
    }

    public CompoundTag serializeWithoutInsertOrder(HolderLookup.Provider registryAccess) {
        return serialize(registryAccess, this::saveWithoutInsertOrder);
    }

    public void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag tag) {
        try (var problemReporter = new ProblemReporter.ScopedCollector(SummoningRituals.LOGGER)) {
            load(TagValueInput.create(problemReporter, registryAccess, tag));
        }
    }

    private static CompoundTag serialize(HolderLookup.Provider registryAccess, Consumer<ValueOutput> serializer) {
        try (var problemReporter = new ProblemReporter.ScopedCollector(SummoningRituals.LOGGER)) {
            var output = TagValueOutput.createWithContext(problemReporter, registryAccess);
            serializer.accept(output);
            return output.buildResult();
        }
    }

    public void save(ValueOutput output) {
        saveWithoutInsertOrder(output);

        var insertOrderOutput = output.list(Constants.INSERT_ORDER, InternalInventory.SLOTTED_STACK_CODEC);
        for (var entry : insertOrder) {
            insertOrderOutput.add(entry);
        }
    }

    public void saveWithoutInsertOrder(ValueOutput output) {
        inventory.serializeNBT(output.child(Constants.INVENTORY));
        output.store(Constants.INITIATOR, ItemStack.OPTIONAL_CODEC, initiator);
    }

    public void load(ValueInput input) {
        inventory.deserializeNBT(input.childOrEmpty(Constants.INVENTORY));

        input.list(Constants.INSERT_ORDER, InternalInventory.SLOTTED_STACK_CODEC).ifPresent(entries -> {
            insertOrder.clear();
            entries.forEach(insertOrder::add);
        });

        initiator = input.read(Constants.INITIATOR, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(getStackInSlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return getStackInSlot(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        if (resource.isEmpty()) return getSlotLimit(index);
        if (!isValid(index, resource)) return 0;

        return Math.min(getSlotLimit(index), resource.getMaxStackSize());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return isValidItem(index, resource.getItem());
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return insert(resource, amount, transaction);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || pendingInsertion.queuedInitiator != null) return 0;

        var stack = resource.toStack(amount);
        var remainder = host.handleItemInsertion(null, stack, true);
        var inserted = amount - remainder.getCount();
        if (inserted <= 0) return 0;

        pendingInsertion.queue(stack.copyWithCount(inserted), host.startsRitual(stack), transaction);
        return inserted;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    public int getSlotLimit(int slot) {
        if (slot == getInitiatorSlot()) return 1;
        return Item.DEFAULT_MAX_STACK_SIZE;
    }

    public ItemStack getStackInSlot(int slot) {
        if (slot == getInitiatorSlot()) return initiator;
        return inventory.get(slot);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot == getInitiatorSlot()) {
            Preconditions.checkArgument(stack.getCount() == 1, "initiator must be a single item");
            initiator = stack;
        } else {
            inventory.set(slot, stack);
        }

        onContentsChanged();
    }

    private boolean isValidItem(int slot, Item item) {
        var recipeMap = host.getRecipeMap();
        if (recipeMap == null) return false;

        if (slot == getInitiatorSlot()) {
            return AltarRecipe.isInitiator(recipeMap, item);
        }

        return AltarRecipe.isInput(recipeMap, item);
    }

    // region RecipeInput delegate
    @Override
    public ItemStack getItem(int index) {
        return getStackInSlot(index);
    }

    @Override
    public int size() {
        return inventory.slots() + 1;
    }
    // endregion RecipeInput delegate

    /**
     * Inserts an input {@link ItemStack} into the inventory while tracking the insertion order.
     * The method iterates the inventory and tries to insert or merge the stack into each slot
     * until the stack is fully inserted or the inventory is full.
     *
     * @param inputStack The input {@link ItemStack} to insert into the inventory. This should not be modified.
     * @param simulate   Specifies whether the insertion is simulated.
     * @return The remaining {@link ItemStack} if the inventory cannot fully accommodate the input stack.
     */
    public ItemStack insertInputTracked(ItemStack inputStack, boolean simulate) {
        var remainingCount = inputStack.getCount();

        for (var slot = 0; slot < inventory.slots(); slot++) {
            var stackToInsert = inputStack.copyWithCount(remainingCount);
            remainingCount = insertOrFillItemInSlot(slot, stackToInsert, simulate);

            if (remainingCount == 0) {
                trackInsert(slot, stackToInsert.copy(), simulate);
                return ItemStack.EMPTY;
            }

            if (remainingCount == stackToInsert.getCount()) {
                continue;
            }

            var insertedCount = stackToInsert.getCount() - remainingCount;
            trackInsert(slot, stackToInsert.copyWithCount(insertedCount), simulate);
        }

        if (remainingCount == 0) {
            return ItemStack.EMPTY;
        }

        return inputStack.copyWithCount(remainingCount);
    }

    /**
     * Attempts to insert the given {@link ItemStack} into a specified slot in the inventory.
     * If the slot is empty, the stack is inserted directly. If the slot already contains items,
     * it tries to merge the stack with the existing stack. Both respect the slot and stack size limits.
     *
     * @param slot     The index of the inventory slot to insert into.
     * @param stack    The {@link ItemStack} to be inserted or merged into the slot. This can safely be used and modified.
     * @param simulate Specifies whether the insertion is simulated.
     * @return The remaining item count that could not be inserted into the slot.
     */
    private int insertOrFillItemInSlot(int slot, ItemStack stack, boolean simulate) {
        var stackInSlot = inventory.get(slot);

        if (!stackInSlot.isEmpty() &&!ItemStack.isSameItemSameComponents(stackInSlot, stack)) {
            return stack.getCount();
        }

        var maxCount = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        var toInsert = Math.min(maxCount - stackInSlot.getCount(), stack.getCount());
        if (toInsert <= 0) return stack.getCount();
        if (simulate) return stack.getCount() - toInsert;

        if (stackInSlot.isEmpty()) {
            setStackInSlot(slot, stack.copyWithCount(toInsert));
        } else {
            stackInSlot.grow(toInsert);
            onContentsChanged();
        }

        return stack.getCount() - toInsert;
    }

    private void trackInsert(int slot, ItemStack stack, boolean simulate) {
        if (simulate) return;
        insertOrder.push(new Tuple<>(stack, slot));
    }

    public void removeLastInsertedItem() {
        if (!initiator.isEmpty()) {
            host.spawnItemAboveAltar(initiator);
            initiator = ItemStack.EMPTY;
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

    public boolean consumeRecipeInputs(ServerLevel level, AltarRecipe recipe) {
        var snapshot = inventory.snapshot();

        var toRemoveTotal = 0;
        var removedTotal = 0;

        for (var input : recipe.inputs().itemInputs()) {
            toRemoveTotal += input.count();
            var alreadyRemoved = 0;

            for (var slot = 0; slot < inventory.slots(); slot++) {
                var stack = inventory.get(slot);
                if (stack.isEmpty() || !input.ingredient().test(stack)) continue;

                var shrinkCount = Math.min(input.count() - alreadyRemoved, stack.getCount());
                stack.shrink(shrinkCount);
                if (stack.isEmpty()) inventory.remove(slot);

                alreadyRemoved += shrinkCount;
                if (alreadyRemoved >= input.count()) break;
            }

            removedTotal += alreadyRemoved;
        }

        if (removedTotal < toRemoveTotal) {
            inventory.restore(snapshot);
            return false;
        }

        initiator = ItemStack.EMPTY;
        rebuildInsertOrder();
        onContentsChanged();
        return true;
    }

    public void dropContents(ServerLevel serverLevel, BlockPos blockPos) {
        for (var stack : inventory) {
            if (stack.isEmpty()) continue;
            dropItem(serverLevel, stack, blockPos);
        }
        inventory.clear();
        insertOrder.clear();

        if (!initiator.isEmpty()) {
            dropItem(serverLevel, initiator, blockPos);
            initiator = ItemStack.EMPTY;
        }

        onContentsChanged();
    }

    private void dropItem(ServerLevel level, ItemStack stack, BlockPos blockPos) {
        var itemEntity = new ItemEntity(level, 0, 0, 0, stack);
        itemEntity.setPos(Vec3.atCenterOf(blockPos));
        level.addFreshEntity(itemEntity);
    }

    private void onContentsChanged() {
        if (suppressChanges) return;
        host.onInventoryChanged(this::serializeWithoutInsertOrder);
    }

    private void rebuildInsertOrder() {
        insertOrder.clear();
        for (var slot = inventory.slots(); slot >= 0; slot--) {
            var stack = getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            trackInsert(slot, stack.copy(), false);
        }
    }

    private int getInitiatorSlot() {
        return inventory.slots();
    }

    public ItemStack getInitiator() {
        return initiator;
    }

    public void setInitiator(ItemStack initiator) {
        this.initiator = initiator;
        onContentsChanged();
    }

    public List<ItemStack> getDisplayItems() {
        var items = new ArrayList<ItemStack>();
        for (var stack : inventory) {
            if (!stack.isEmpty()) items.add(stack);
        }
        return items;
    }

    private record PendingState(
        ItemStack[] items, List<Tuple<ItemStack, Integer>> insertOrder, @Nullable ItemStack queuedInitiator, boolean changed
    ) {}

    private final class PendingInsertion extends SnapshotJournal<PendingState> {

        @Nullable
        private ItemStack queuedInitiator;
        private boolean changed;

        private void queue(ItemStack stack, boolean startsRitual, TransactionContext transaction) {
            updateSnapshots(transaction);
            if (startsRitual) {
                queuedInitiator = stack;
                return;
            }

            suppressChanges = true;
            try {
                insertInputTracked(stack, false);
            } finally {
                suppressChanges = false;
            }
            changed = true;
        }

        @Override
        protected PendingState createSnapshot() {
            var insertOrderCopy = new ArrayList<Tuple<ItemStack, Integer>>();
            for (var entry : insertOrder) {
                insertOrderCopy.add(new Tuple<>(entry.getA().copy(), entry.getB()));
            }
            return new PendingState(inventory.snapshot(), insertOrderCopy, queuedInitiator, changed);
        }

        @Override
        protected void revertToSnapshot(PendingState snapshot) {
            inventory.restore(snapshot.items());
            insertOrder.clear();
            insertOrder.addAll(snapshot.insertOrder());
            queuedInitiator = snapshot.queuedInitiator();
            changed = snapshot.changed();
        }

        @Override
        protected void onRootCommit(PendingState originalState) {
            var toInsert = queuedInitiator;
            var wasChanged = changed;
            queuedInitiator = null;
            changed = false;

            if (wasChanged) onContentsChanged();
            if (toInsert == null) return;

            var remainder = host.handleItemInsertion(null, toInsert, false);
            if (!remainder.isEmpty()) host.spawnItemAboveAltar(remainder);
        }
    }
}

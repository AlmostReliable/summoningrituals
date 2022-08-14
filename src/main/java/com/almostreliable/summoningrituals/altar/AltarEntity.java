package com.almostreliable.summoningrituals.altar;

import com.almostreliable.summoningrituals.Setup;
import com.almostreliable.summoningrituals.inventory.AltarInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AltarEntity extends BlockEntity {

    private static final String INVENTORY = "inventory";

    private final AltarInventory inventory;
    private final LazyOptional<AltarInventory> inventoryCap;

    public AltarEntity(BlockPos pos, BlockState state) {
        super(Setup.ALTAR_ENTITY.get(), pos, state);
        inventory = new AltarInventory(this);
        inventoryCap = LazyOptional.of(() -> inventory);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(INVENTORY)) inventory.deserializeNBT(tag.getCompound(INVENTORY));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(INVENTORY, inventory.serializeNBT());
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryCap.invalidate();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (!remove && cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) {
            return inventoryCap.cast();
        }
        return super.getCapability(cap, side);
    }

    public void tick() {
        // TODO
        if (level == null || level.isClientSide) return;
    }

    private void changeActivityState(boolean state) {
        if (level == null || level.isClientSide) return;
        var oldState = level.getBlockState(worldPosition);
        if (!oldState.getValue(AltarBlock.ACTIVE).equals(state)) {
            level.setBlockAndUpdate(worldPosition, oldState.setValue(AltarBlock.ACTIVE, state));
        }
    }
}

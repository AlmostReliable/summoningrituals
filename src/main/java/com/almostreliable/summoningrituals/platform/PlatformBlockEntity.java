package com.almostreliable.summoningrituals.platform;

import com.almostreliable.summoningrituals.inventory.AltarInventory;
import com.almostreliable.summoningrituals.inventory.ItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PlatformBlockEntity extends BlockEntity {

    protected final AltarInventory inventory;
    private final LazyOptional<ItemHandler> inventoryCap;

    protected int progress;

    protected PlatformBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        this.inventory = new AltarInventory(this);
        this.inventoryCap = LazyOptional.of(() -> inventory);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryCap.invalidate();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (!remove && cap.equals(ForgeCapabilities.ITEM_HANDLER) && progress == 0) {
            return inventoryCap.cast();
        }
        return super.getCapability(cap, side);
    }

    public abstract ItemStack handleInteraction(@Nullable ServerPlayer player, ItemStack stack);

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public AltarInventory getInventory() {
        return inventory;
    }
}

package com.almostreliable.summoningrituals.platform;

import com.almostreliable.summoningrituals.inventory.AltarInventory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

@SuppressWarnings("UnstableApiUsage")
public abstract class PlatformBlockEntity extends BlockEntity {

    protected final AltarInventory inventory;

    protected int progress;

    protected PlatformBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        this.inventory = new AltarInventory(this);
    }

    @Nullable
    public Storage<ItemVariant> exposeStorage() {
        if (!remove && progress == 0) {
            return inventory.getStorageInv();
        }
        return null;
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

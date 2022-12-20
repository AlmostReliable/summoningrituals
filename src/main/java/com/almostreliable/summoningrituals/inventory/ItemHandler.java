package com.almostreliable.summoningrituals.inventory;

import com.almostreliable.summoningrituals.platform.TagSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A simple representation of an inventory modified to be compatible with an Altar.
 * <p>
 * Taken from Minecraft Forge's {@code IItemHandler} and {@code IItemHandlerModifiable}.
 */
public interface ItemHandler extends TagSerializable<CompoundTag> {

    int getSlots();

    ItemStack getStackInSlot(int slot);

    void setStackInSlot(int slot, ItemStack stack);

    int getSlotLimit(int slot);

    boolean isItemValid(int slot, ItemStack stack);

    List<ItemStack> getNoneEmptyItems();

    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    ItemStack getCatalyst();
}

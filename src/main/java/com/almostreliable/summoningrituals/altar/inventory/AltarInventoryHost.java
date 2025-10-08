package com.almostreliable.summoningrituals.altar.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface AltarInventoryHost {

    ItemStack handleItemInsertion(@Nullable ServerPlayer player, ItemStack stack, boolean simulate);

    void spawnItemAboveAltar(ItemStack stack);

    void onInventoryChanged(Function<HolderLookup.Provider, CompoundTag> serializer);
}

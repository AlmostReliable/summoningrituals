package com.almostreliable.summoningrituals.altar.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface AltarInventoryHost {

    /**
     * Handles the insertion of an {@link ItemStack} into the altar inventory. This is called when interacting with the block
     * or from capabilities trying to push into the inventory.
     * <p>
     * Checks whether a recipe is already in progress and blocks the insertion if so. Delegates to initiator and
     * input insertion methods depending on the item being inserted.
     *
     * @param player   The player initiating the item insertion, or null if no player is associated e.g., on automation.
     * @param stack    The {@link ItemStack} being inserted. This should not be modified.
     * @param simulate Specifies whether the insertion is simulated.
     * @return The remaining {@link ItemStack} after attempting the insertion, or an empty stack if the entire stack was inserted.
     */
    ItemStack handleItemInsertion(@Nullable ServerPlayer player, ItemStack stack, boolean simulate);

    void spawnItemAboveAltar(ItemStack stack);

    void onInventoryChanged(Function<HolderLookup.Provider, CompoundTag> serializer);

    @Nullable RecipeManager getRecipeManager();
}

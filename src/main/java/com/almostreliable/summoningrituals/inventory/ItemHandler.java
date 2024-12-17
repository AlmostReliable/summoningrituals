package com.almostreliable.summoningrituals.inventory;

import com.almostreliable.summoningrituals.platform.TagSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.List;

/**
 * A simple representation of an inventory modified to be compatible with an Altar.
 * <p>
 * Taken from Minecraft Forge's {@code IItemHandler} and {@code IItemHandlerModifiable}.
 */
public interface ItemHandler extends IItemHandlerModifiable, TagSerializable<CompoundTag> {

    List<ItemStack> getNoneEmptyItems();

    ItemStack getCatalyst();
}

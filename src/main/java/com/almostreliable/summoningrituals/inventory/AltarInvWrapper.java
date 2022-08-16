package com.almostreliable.summoningrituals.inventory;

import com.almostreliable.summoningrituals.altar.AltarEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class AltarInvWrapper extends RecipeWrapper {

    private final AltarInventory delegate;

    AltarInvWrapper(AltarInventory inv) {
        super(inv);
        delegate = inv;
    }

    public AltarEntity getParent() {
        return delegate.getParent();
    }

    public NonNullList<ItemStack> getInputs() {
        return delegate.getInputs();
    }

    public ItemStack getCatalyst() {
        return delegate.getCatalyst();
    }
}

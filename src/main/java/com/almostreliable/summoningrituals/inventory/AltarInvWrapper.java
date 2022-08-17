package com.almostreliable.summoningrituals.inventory;

import com.almostreliable.summoningrituals.altar.AltarEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.List;

public class AltarInvWrapper extends RecipeWrapper {

    private final AltarInventory delegate;

    AltarInvWrapper(AltarInventory inv) {
        super(inv);
        delegate = inv;
    }

    public AltarEntity getParent() {
        return delegate.getParent();
    }

    public List<ItemStack> getInputs() {
        return delegate.getItems();
    }

    public ItemStack getCatalyst() {
        return delegate.getCatalyst();
    }
}

package com.almostreliable.summoningrituals.inventory;

import com.almostreliable.summoningrituals.altar.AltarEntity;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class AltarInvWrapper extends RecipeWrapper {

    private final AltarEntity parent;

    AltarInvWrapper(AltarInventory inv, AltarEntity parent) {
        super(inv);
        this.parent = parent;
    }

    public AltarEntity getParent() {
        return parent;
    }
}

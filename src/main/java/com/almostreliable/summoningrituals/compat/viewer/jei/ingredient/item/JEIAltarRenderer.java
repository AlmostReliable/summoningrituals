package com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.item;

import com.almostreliable.summoningrituals.compat.viewer.common.AltarRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.world.item.ItemStack;

public class JEIAltarRenderer extends AltarRenderer implements IIngredientRenderer<ItemStack> {
    public JEIAltarRenderer(int size) {
        super(size);
    }

    @Override
    public int getWidth() {
        return size;
    }

    @Override
    public int getHeight() {
        return size;
    }
}

package com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.item;

import com.almostreliable.summoningrituals.compat.viewer.common.CatalystRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.world.item.ItemStack;

public class JEICatalystRenderer extends CatalystRenderer implements IIngredientRenderer<ItemStack> {

    public JEICatalystRenderer(int size) {
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

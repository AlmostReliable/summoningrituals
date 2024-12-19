package com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.mob;

import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.common.MobRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;

public class JEIMobRenderer extends MobRenderer implements IIngredientRenderer<EntityIngredient> {

    public JEIMobRenderer(int size) {
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

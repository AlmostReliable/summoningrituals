package com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.block;

import com.almostreliable.summoningrituals.compat.viewer.common.BlockReferenceRenderer;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import mezz.jei.api.ingredients.IIngredientRenderer;

public class JEIBlockReferenceRenderer extends BlockReferenceRenderer implements IIngredientRenderer<BlockReference> {

    public JEIBlockReferenceRenderer(int size) {
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

package com.almostreliable.summoningrituals.compat.viewer.jei.widget;

import com.almostreliable.summoningrituals.compat.viewer.common.RecipeViewerAltarLayout;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;

import net.minecraft.client.gui.navigation.ScreenRectangle;

import mezz.jei.api.gui.inputs.IJeiGuiEventListener;

public class JeiPatternClickListener implements IJeiGuiEventListener {

    private final RecipeViewerAltarLayout layout;
    private final AltarRecipe recipe;

    public JeiPatternClickListener(RecipeViewerAltarLayout layout, AltarRecipe recipe) {
        this.layout = layout;
        this.recipe = recipe;
    }

    @Override
    public ScreenRectangle getArea() {
        return new ScreenRectangle(
            2,
            RecipeViewerAltarLayout.TEXTURE_HEIGHT - RecipeViewerAltarLayout.SLOT_SIZE * 2 - 5,
            RecipeViewerAltarLayout.SLOT_SIZE,
            RecipeViewerAltarLayout.SLOT_SIZE
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        layout.onPreviewButtonClicked(recipe);
        return true;
    }
}

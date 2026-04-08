package com.almostreliable.summoningrituals.client.render;

import com.almostreliable.summoningrituals.recipe.AltarRecipe;

@FunctionalInterface
public interface CustomRitualRenderer {

    void render(AltarRenderer renderer, AltarRecipe recipe, AltarRenderContext context);
}

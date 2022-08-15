package com.almostreliable.summoningrituals.recipe;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

public class FinishedAltarRecipe implements FinishedRecipe {

    private final AltarRecipe recipe;

    public FinishedAltarRecipe(AltarRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {

    }

    @Override
    public ResourceLocation getId() {
        return recipe.getId();
    }

    @Override
    public RecipeSerializer<?> getType() {
        return recipe.getSerializer();
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return null;
    }
}

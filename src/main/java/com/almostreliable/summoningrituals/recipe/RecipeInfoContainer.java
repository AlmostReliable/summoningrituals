package com.almostreliable.summoningrituals.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public record RecipeInfoContainer(
    ResourceLocation recipeId,
    AltarRecipe recipe,
    List<Entity> inputEntities,
    List<ItemEntity> outputItems,
    List<Entity> outputEntities
) {

    public static RecipeInfoContainer inputInfo(RecipeHolder<AltarRecipe> recipe, List<Entity> inputEntities) {
        return new RecipeInfoContainer(recipe.id(), recipe.value(), inputEntities, List.of(), List.of());
    }

    public static RecipeInfoContainer outputInfo(RecipeInfoContainer inputInfo, List<ItemEntity> outputItems, List<Entity> outputEntities) {
        return new RecipeInfoContainer(inputInfo.recipeId, inputInfo.recipe, inputInfo.inputEntities, outputItems, outputEntities);
    }

    @Override
    public int hashCode() {
        return recipeId.hashCode();
    }
}

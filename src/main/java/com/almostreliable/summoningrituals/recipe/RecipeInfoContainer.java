package com.almostreliable.summoningrituals.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Collection;
import java.util.List;

public record RecipeInfoContainer(
    ResourceLocation recipeId,
    AltarRecipe recipe,
    Collection<Entity> inputEntities,
    Collection<ItemEntity> outputItems,
    Collection<Entity> outputEntities
) {

    public static RecipeInfoContainer inputInfo(RecipeHolder<AltarRecipe> recipe, Collection<Entity> inputEntities) {
        return new RecipeInfoContainer(recipe.id(), recipe.value(), inputEntities, List.of(), List.of());
    }

    public static RecipeInfoContainer outputInfo(
        RecipeInfoContainer inputInfo, Collection<ItemEntity> outputItems, Collection<Entity> outputEntities
    ) {
        return new RecipeInfoContainer(inputInfo.recipeId, inputInfo.recipe, inputInfo.inputEntities, outputItems, outputEntities);
    }

    @Override
    public int hashCode() {
        return recipeId.hashCode();
    }
}

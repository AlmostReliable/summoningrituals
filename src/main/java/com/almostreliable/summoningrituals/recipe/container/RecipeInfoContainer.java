package com.almostreliable.summoningrituals.recipe.container;

import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.AltarRecipeSerializer;
import com.almostreliable.summoningrituals.recipe.condition.custom.BlockPatternCheck;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.RecipeHolder;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public record RecipeInfoContainer(
    ResourceLocation getRecipeId,
    AltarRecipe getRecipe,
    Collection<Entity> getInputEntities,
    Collection<ItemEntity> getOutputItems,
    Collection<Entity> getOutputEntities
) {

    // only used to sync recipe info to custom renderers
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeInfoContainer> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, RecipeInfoContainer::getRecipeId,
        AltarRecipeSerializer.STREAM_CODEC, RecipeInfoContainer::getRecipe,
        (recipeId, recipe) -> new RecipeInfoContainer(recipeId, recipe, List.of(), List.of(), List.of())
    );

    public static RecipeInfoContainer inputInfo(RecipeHolder<AltarRecipe> recipe, Collection<Entity> inputEntities) {
        return new RecipeInfoContainer(recipe.id(), recipe.value(), inputEntities, List.of(), List.of());
    }

    public static RecipeInfoContainer outputInfo(
        RecipeInfoContainer inputInfo, Collection<ItemEntity> outputItems, Collection<Entity> outputEntities
    ) {
        return new RecipeInfoContainer(inputInfo.getRecipeId, inputInfo.getRecipe, inputInfo.getInputEntities, outputItems, outputEntities);
    }

    @Override
    public int hashCode() {
        return getRecipeId.hashCode();
    }

    @Nullable
    public BlockPatternCheck getBlockPatternCondition() {
        return getRecipe.getBlockPatternCondition();
    }
}

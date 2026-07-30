package com.almostreliable.summoningrituals.recipe.container;

import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.AltarRecipeSerializer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Collection;
import java.util.List;

public record RecipeInfo(
    ResourceLocation getRecipeId, AltarRecipe getRecipe, ItemStack getInitiator, Collection<Entity> getInputEntities,
    boolean isBlockPatternExtensionMatched, Collection<ItemEntity> getOutputItems, Collection<Entity> getOutputEntities
) {

    // only used to sync recipe info to custom renderers
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeInfo> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, RecipeInfo::getRecipeId,
        AltarRecipeSerializer.STREAM_CODEC, RecipeInfo::getRecipe,
        (recipeId, recipe) -> new RecipeInfo(recipeId, recipe, ItemStack.EMPTY, List.of(), false, List.of(), List.of())
    );

    public static RecipeInfo inputInfo(RecipeHolder<AltarRecipe> recipe, ItemStack initiator, Collection<Entity> inputEntities) {
        return new RecipeInfo(recipe.id(), recipe.value(), initiator, inputEntities, false, List.of(), List.of());
    }

    public static RecipeInfo blockPatternInfo(RecipeInfo inputInfo, boolean blockPatternExtensionMatched) {
        return new RecipeInfo(
            inputInfo.getRecipeId,
            inputInfo.getRecipe,
            inputInfo.getInitiator,
            inputInfo.getInputEntities,
            blockPatternExtensionMatched,
            List.of(),
            List.of()
        );
    }

    public static RecipeInfo outputInfo(
        RecipeInfo blockPatternInfo, Collection<ItemEntity> outputItems, Collection<Entity> outputEntities) {
        return new RecipeInfo(
            blockPatternInfo.getRecipeId, blockPatternInfo.getRecipe, blockPatternInfo.getInitiator, blockPatternInfo.getInputEntities,
            blockPatternInfo.isBlockPatternExtensionMatched, outputItems, outputEntities
        );
    }

    @Override
    public int hashCode() {
        return getRecipeId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RecipeInfo recipeInfo && getRecipeId.equals(recipeInfo.getRecipeId);
    }
}

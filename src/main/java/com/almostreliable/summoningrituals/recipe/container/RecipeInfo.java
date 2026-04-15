package com.almostreliable.summoningrituals.recipe.container;

import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.AltarRecipeSerializer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Collection;
import java.util.List;

public record RecipeInfo(
    ResourceLocation recipeId, AltarRecipe recipe, Collection<Entity> inputEntities, boolean blockPatternExtensionMatched,
    Collection<ItemEntity> outputItems, Collection<Entity> outputEntities
) {

    // only used to sync recipe info to custom renderers
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeInfo> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, RecipeInfo::recipeId,
        AltarRecipeSerializer.STREAM_CODEC, RecipeInfo::recipe,
        (recipeId, recipe) -> new RecipeInfo(recipeId, recipe, List.of(), false, List.of(), List.of())
    );

    public static RecipeInfo inputInfo(RecipeHolder<AltarRecipe> recipe, Collection<Entity> inputEntities) {
        return new RecipeInfo(recipe.id(), recipe.value(), inputEntities, false, List.of(), List.of());
    }

    public static RecipeInfo blockPatternInfo(RecipeInfo inputInfo, boolean blockPatternExtensionMatched) {
        return new RecipeInfo(
            inputInfo.recipeId,
            inputInfo.recipe,
            inputInfo.inputEntities,
            blockPatternExtensionMatched,
            List.of(),
            List.of()
        );
    }

    public static RecipeInfo outputInfo(
        RecipeInfo blockPatternInfo, Collection<ItemEntity> outputItems, Collection<Entity> outputEntities) {
        return new RecipeInfo(
            blockPatternInfo.recipeId, blockPatternInfo.recipe, blockPatternInfo.inputEntities,
            blockPatternInfo.blockPatternExtensionMatched, outputItems, outputEntities
        );
    }

    @Override
    public int hashCode() {
        return recipeId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RecipeInfo recipeInfo && recipeId.equals(recipeInfo.recipeId);
    }
}

package com.almostreliable.summoningrituals.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

public class AltarRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<AltarRecipe> {

    @Override
    public AltarRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        var output = RecipeOutput.fromJson(json.get("output").getAsJsonObject());

        return null;
    }

    @Nullable
    @Override
    public AltarRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        return null;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AltarRecipe recipe) {

    }
}

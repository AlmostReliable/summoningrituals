package com.almostreliable.summoningrituals.recipe;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

public class AltarRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<AltarRecipe> {

    private static final String INPUTS = "inputs";
    private static final String CATALYST = "catalyst";
    public static final String RECIPE_TIME = "recipe_time";
    public static final String DAY_TIME = "day_time";
    public static final String BLOCK_BELOW = "block_below";

    @Override
    public AltarRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        var output = RecipeOutput.fromJson(json.get("output").getAsJsonObject());
        NonNullList<Ingredient> inputs = NonNullList.create();
        if (json.has(INPUTS)) {
            var inputsJson = json.get(INPUTS).getAsJsonArray();
            for (var inputJson : inputsJson) {
                inputs.add(Ingredient.fromJson(inputJson.getAsJsonObject()));
            }
        }
        var catalyst = Ingredient.fromJson(json.get(CATALYST).getAsJsonObject());
        var recipeTime = GsonHelper.getAsInt(json, RECIPE_TIME, 100);
        var dayTime = GsonHelper.getAsInt(json, DAY_TIME, -1);
        var blockBelow = BlockPredicate.fromJson(json.get(BLOCK_BELOW));
        // var blockBelow =
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

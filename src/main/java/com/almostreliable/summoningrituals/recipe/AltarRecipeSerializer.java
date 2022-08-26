package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.recipe.AltarRecipe.DAY_TIME;
import com.almostreliable.summoningrituals.recipe.AltarRecipe.WEATHER;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

public class AltarRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<AltarRecipe> {

    @Override
    public AltarRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        var outputs = RecipeOutputs.fromJson(json.getAsJsonArray(Constants.OUTPUTS));

        NonNullList<IngredientStack> inputs = NonNullList.create();
        if (json.has(Constants.INPUT)) {
            var inputsJson = json.getAsJsonArray(Constants.INPUT);
            for (var inputJson : inputsJson) {
                inputs.add(IngredientStack.fromJson(inputJson.getAsJsonObject()));
            }
        }

        var catalyst = Ingredient.fromJson(json.getAsJsonObject(Constants.CATALYST));
        AltarRecipe.CATALYST_CACHE.add(catalyst);

        RecipeSacrifices sacrifices = null;
        if (json.has(Constants.SACRIFICES)) {
            sacrifices = RecipeSacrifices.fromJson(json.getAsJsonObject(Constants.SACRIFICES));
        }

        var recipeTime = GsonHelper.getAsInt(json, Constants.RECIPE_TIME, 100);

        BlockReference blockBelow = null;
        if (json.has(Constants.BLOCK_BELOW)) {
            blockBelow = BlockReference.fromJson(json.getAsJsonObject(Constants.BLOCK_BELOW));
        }

        var dayTime = DAY_TIME.valueOf(
            GsonHelper.getAsString(json, Constants.DAY_TIME, DAY_TIME.ANY.name()).toUpperCase()
        );

        var weather = WEATHER.valueOf(
            GsonHelper.getAsString(json, Constants.WEATHER, WEATHER.ANY.name()).toUpperCase()
        );

        return new AltarRecipe(
            recipeId,
            outputs,
            inputs,
            catalyst,
            sacrifices,
            recipeTime,
            blockBelow,
            dayTime,
            weather
        );
    }

    @Nullable
    @Override
    public AltarRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        var outputs = RecipeOutputs.fromNetwork(buffer);

        NonNullList<IngredientStack> inputs = NonNullList.create();
        var inputCount = buffer.readVarInt();
        for (var i = 0; i < inputCount; i++) {
            inputs.add(IngredientStack.fromNetwork(buffer));
        }

        var catalyst = Ingredient.fromNetwork(buffer);

        RecipeSacrifices sacrifices = null;
        if (buffer.readBoolean()) {
            sacrifices = RecipeSacrifices.fromNetwork(buffer);
        }

        var recipeTime = buffer.readInt();

        BlockReference blockBelow = null;
        if (buffer.readBoolean()) {
            blockBelow = BlockReference.fromNetwork(buffer);
        }

        var dayTime = DAY_TIME.values()[buffer.readVarInt()];
        var weather = WEATHER.values()[buffer.readVarInt()];

        return new AltarRecipe(
            recipeId,
            outputs,
            inputs,
            catalyst,
            sacrifices,
            recipeTime,
            blockBelow,
            dayTime,
            weather
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AltarRecipe recipe) {
        recipe.getOutputs().toNetwork(buffer);

        buffer.writeVarInt(recipe.getInputs().size());
        for (var input : recipe.getInputs()) {
            input.toNetwork(buffer);
        }

        recipe.getCatalyst().toNetwork(buffer);

        if (recipe.getSacrifices() != null) {
            buffer.writeBoolean(true);
            recipe.getSacrifices().toNetwork(buffer);
        } else {
            buffer.writeBoolean(false);
        }

        buffer.writeInt(recipe.getRecipeTime());

        if (recipe.getBlockBelow() != null) {
            buffer.writeBoolean(true);
            recipe.getBlockBelow().toNetwork(buffer);
        } else {
            buffer.writeBoolean(false);
        }

        buffer.writeVarInt(recipe.getDayTime().ordinal());
        buffer.writeVarInt(recipe.getWeather().ordinal());
    }
}

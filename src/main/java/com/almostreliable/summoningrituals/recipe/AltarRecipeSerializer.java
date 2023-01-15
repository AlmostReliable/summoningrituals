package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.inventory.AltarInventory;
import com.almostreliable.summoningrituals.recipe.AltarRecipe.DAY_TIME;
import com.almostreliable.summoningrituals.recipe.AltarRecipe.WEATHER;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.recipe.component.IngredientStack;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs;
import com.almostreliable.summoningrituals.recipe.component.RecipeSacrifices;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class AltarRecipeSerializer implements RecipeSerializer<AltarRecipe> {

    @Override
    public AltarRecipe fromJson(ResourceLocation id, JsonObject json) {
        var catalyst = Ingredient.fromJson(json.getAsJsonObject(Constants.CATALYST));
        AltarRecipe.CATALYST_CACHE.add(catalyst);

        var outputs = RecipeOutputs.fromJson(json.getAsJsonArray(Constants.OUTPUTS));

        NonNullList<IngredientStack> inputs = NonNullList.create();
        if (json.has(Constants.INPUTS)) {
            var inputJson = json.get(Constants.INPUTS);
            if (inputJson.isJsonObject) {
                addInput(inputs, IngredientStack.fromJson(inputJson));
            } else {
                for (var input : inputJson.asJsonArray) {
                    addInput(inputs, IngredientStack.fromJson(input.asJsonObject));
                }
            }
        }

        RecipeSacrifices sacrifices = new RecipeSacrifices();
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
            id,
            catalyst,
            outputs,
            inputs,
            sacrifices,
            recipeTime,
            blockBelow,
            dayTime,
            weather
        );
    }

    @Override
    public AltarRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        var catalyst = Ingredient.fromNetwork(buffer);

        var outputs = RecipeOutputs.fromNetwork(buffer);

        NonNullList<IngredientStack> inputs = NonNullList.create();
        var inputCount = buffer.readVarInt();
        for (var i = 0; i < inputCount; i++) {
            inputs.add(IngredientStack.fromNetwork(buffer));
        }

        RecipeSacrifices sacrifices = new RecipeSacrifices();
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
            id,
            catalyst,
            outputs,
            inputs,
            sacrifices,
            recipeTime,
            blockBelow,
            dayTime,
            weather
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AltarRecipe recipe) {
        recipe.catalyst.toNetwork(buffer);

        recipe.outputs.toNetwork(buffer);

        buffer.writeVarInt(recipe.inputs.size());
        for (var input : recipe.inputs) {
            input.toNetwork(buffer);
        }

        if (recipe.sacrifices.isEmpty) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            recipe.sacrifices.toNetwork(buffer);
        }

        buffer.writeInt(recipe.recipeTime);

        if (recipe.blockBelow != null) {
            buffer.writeBoolean(true);
            recipe.blockBelow.toNetwork(buffer);
        } else {
            buffer.writeBoolean(false);
        }

        buffer.writeVarInt(recipe.dayTime.ordinal());
        buffer.writeVarInt(recipe.weather.ordinal());
    }

    private void addInput(NonNullList<IngredientStack> inputs, IngredientStack input) {
        if (inputs.size() >= AltarInventory.SIZE) {
            throw new IllegalArgumentException("Too many inputs, max is " + AltarInventory.SIZE);
        }
        inputs.add(input);
    }
}

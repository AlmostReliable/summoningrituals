package com.almostreliable.summoningrituals.recipe.component;

import com.almostreliable.summoningrituals.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;

public record IngredientStack(Ingredient ingredient, int count) {

    public static IngredientStack fromJson(JsonElement json) {
        IngredientStack ingredientStack;
        if (json instanceof JsonObject jsonObject && jsonObject.has(Constants.COUNT)) {
            var ingredient = Ingredient.fromJson(jsonObject.has(Constants.INGREDIENT)
                ? jsonObject.get(Constants.INGREDIENT) : jsonObject);
            var count = GsonHelper.getAsInt(jsonObject, Constants.COUNT);
            ingredientStack = new IngredientStack(ingredient, count);
        } else {
            ingredientStack = new IngredientStack(Ingredient.fromJson(json), 1);
        }
        if (ingredientStack.ingredient.items.length == 0) {
            throw new IllegalArgumentException("Ingredient is empty, maybe wrong tag");
        }
        return ingredientStack;
    }

    public static IngredientStack fromNetwork(FriendlyByteBuf buffer) {
        var ingred = Ingredient.fromNetwork(buffer);
        var count = buffer.readVarInt();
        return new IngredientStack(ingred, count);
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        ingredient.toNetwork(buffer);
        buffer.writeVarInt(count);
    }
}

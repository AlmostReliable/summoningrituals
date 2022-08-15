package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;

public record IngredientStack(Ingredient ingredient, int count) {

    public static IngredientStack fromJson(JsonObject json) {
        var ingred = Ingredient.fromJson(json);
        var count = GsonHelper.getAsInt(json, Constants.COUNT, 1);
        return new IngredientStack(ingred, count);
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

    public JsonElement toJson(JsonObject json) {
        var ingred = ingredient.toJson();
        if (count > 1) ingred.getAsJsonObject().addProperty(Constants.COUNT, count);
        return ingred;
    }
}

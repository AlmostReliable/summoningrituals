package com.almostreliable.summoningrituals.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;

public record IngredientStack(Ingredient ingredient, int count) {

    private static final String COUNT = "count";

    public static IngredientStack fromJson(JsonObject json) {
        var ingred = Ingredient.fromJson(json);
        var count = GsonHelper.getAsInt(json, COUNT, 1);
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
        if (count > 1) ingred.getAsJsonObject().addProperty(COUNT, count);
        return ingred;
    }
}

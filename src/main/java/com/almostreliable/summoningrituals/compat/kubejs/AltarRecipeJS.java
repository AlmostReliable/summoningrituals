package com.almostreliable.summoningrituals.compat.kubejs;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.BlockReference;
import com.almostreliable.summoningrituals.recipe.RecipeOutputs;
import com.almostreliable.summoningrituals.recipe.RecipeOutputs.ItemOutputBuilder;
import com.almostreliable.summoningrituals.recipe.RecipeOutputs.MobOutputBuilder;
import com.almostreliable.summoningrituals.recipe.RecipeSacrifices;
import com.almostreliable.summoningrituals.util.Bruhtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AltarRecipeJS extends RecipeJS {

    private final RecipeOutputs outputs = new RecipeOutputs();
    private final List<IngredientJS> inputs = new ArrayList<>();
    private final RecipeSacrifices sacrifices = new RecipeSacrifices();
    private boolean serialize;
    private IngredientJS catalyst;
    private final int recipeTime = 100;
    @Nullable private BlockReference blockBelow;
    private AltarRecipe.DAY_TIME dayTime = AltarRecipe.DAY_TIME.ANY;
    private AltarRecipe.WEATHER weather = AltarRecipe.WEATHER.ANY;

    @Override
    public void create(ListJS listJS) {
        if (listJS.size() != 1) {
            throw new IllegalArgumentException("missing catalyst for AltarRecipeJS");
        }
        this.catalyst = IngredientJS.of(listJS.get(0));
        serialize = true;
    }

    @Override
    public void deserialize() {}

    @Override
    public void serialize() {
        if (!serialize) return;

        json = new JsonObject();
        json.add(Constants.CATALYST, catalyst.toJson());
        json.add(Constants.OUTPUTS, outputs.toJson());
        JsonArray inputsArray = new JsonArray();
        inputs.forEach(i -> inputsArray.add(i.toJson()));
        json.add(Constants.INPUT, inputsArray);
        json.add(Constants.SACRIFICES, sacrifices.toJson());
        json.addProperty(Constants.RECIPE_TIME, recipeTime);
        if (blockBelow != null) {
            json.add(Constants.BLOCK_BELOW, blockBelow.toJson());
        }
        json.addProperty(Constants.DAY_TIME, dayTime.name());
        json.addProperty(Constants.WEATHER, weather.name());
    }

    public AltarRecipeJS itemOutput(ItemOutputBuilder itemOutput) {
        outputs.add(itemOutput.build());
        return this;
    }

    public AltarRecipeJS mobOutput(MobOutputBuilder entityOutput) {
        outputs.add(entityOutput.build());
        return this;
    }

    public AltarRecipeJS input(IngredientJS ingredient) {
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException("ingredient is empty");
        }
        inputs.add(ingredient);
        return this;
    }

    public AltarRecipeJS blockBelow(Block block, JsonObject properties) {
        var blockJson = new JsonObject();
        blockJson.addProperty(Constants.BLOCK, Bruhtils.getId(block).toString());
        blockJson.add(Constants.PROPERTIES, properties);
        blockBelow = BlockReference.fromJson(blockJson);
        return this;
    }

    public AltarRecipeJS blockBelow(Block block) {
        return blockBelow(block, new JsonObject());
    }

    public AltarRecipeJS dayTime(AltarRecipe.DAY_TIME dayTime) {
        this.dayTime = dayTime;
        return this;
    }

    public AltarRecipeJS weather(AltarRecipe.WEATHER weather) {
        this.weather = weather;
        return this;
    }
}

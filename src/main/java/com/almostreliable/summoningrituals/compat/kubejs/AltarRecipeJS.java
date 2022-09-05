package com.almostreliable.summoningrituals.compat.kubejs;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs.ItemOutputBuilder;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs.MobOutputBuilder;
import com.almostreliable.summoningrituals.recipe.component.RecipeSacrifices;
import com.almostreliable.summoningrituals.util.SerializeUtils;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.ListJS;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class AltarRecipeJS extends RecipeJS {

    private final RecipeOutputs outputs = new RecipeOutputs();
    private final List<IngredientJS> inputs = new ArrayList<>();
    private final RecipeSacrifices sacrifices = new RecipeSacrifices();
    private IngredientJS catalyst;
    private int recipeTime = 100;
    @Nullable private BlockReference blockBelow;
    private AltarRecipe.DAY_TIME dayTime = AltarRecipe.DAY_TIME.ANY;
    private AltarRecipe.WEATHER weather = AltarRecipe.WEATHER.ANY;

    private boolean serialize;

    @Override
    public void create(ListJS listJS) {
        if (listJS.size() != 1) {
            throw new IllegalArgumentException("missing catalyst");
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
        if (!inputsArray.isEmpty()) {
            if (inputsArray.size() == 1) {
                json.add(Constants.INPUT, inputsArray.get(0));
            } else {
                json.add(Constants.INPUT, inputsArray);
            }
        }
        if (!sacrifices.isEmpty()) {
            json.add(Constants.SACRIFICES, sacrifices.toJson());
        }
        if (recipeTime != 100) {
            json.addProperty(Constants.RECIPE_TIME, recipeTime);
        }
        if (blockBelow != null) {
            json.add(Constants.BLOCK_BELOW, blockBelow.toJson());
        }
        if (dayTime != AltarRecipe.DAY_TIME.ANY) {
            json.addProperty(Constants.DAY_TIME, dayTime.name());
        }
        if (weather != AltarRecipe.WEATHER.ANY) {
            json.addProperty(Constants.WEATHER, weather.name());
        }
        // TODO: remove
        // debug
        ConsoleJS.SERVER.info("Altar Recipe: " + json.toString());
    }

    public AltarRecipeJS itemOutput(ItemOutputBuilder itemOutput) {
        outputs.add(itemOutput.build());
        return this;
    }

    public AltarRecipeJS mobOutput(MobOutputBuilder entityOutput) {
        outputs.add(entityOutput.build());
        return this;
    }

    public AltarRecipeJS input(IngredientJS... ingredients) {
        for (var ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                throw new IllegalArgumentException("ingredient is empty");
            }
            inputs.add(ingredient);
        }
        return this;
    }

    public AltarRecipeJS sacrificeRegion(int width, int height) {
        sacrifices.setRegion(new Vec3i(width, height, width));
        return this;
    }

    public AltarRecipeJS sacrifice(ResourceLocation id, int count) {
        Preconditions.checkNotNull(id);
        sacrifices.add(SerializeUtils.mobFromId(id), count);
        return this;
    }

    public AltarRecipeJS sacrifice(ResourceLocation id) {
        Preconditions.checkNotNull(id);
        return sacrifice(id, 1);
    }

    public AltarRecipeJS recipeTime(int recipeTime) {
        this.recipeTime = recipeTime;
        return this;
    }

    public AltarRecipeJS blockBelow(ResourceLocation id, JsonObject properties) {
        Preconditions.checkNotNull(id);
        var blockJson = new JsonObject();
        blockJson.addProperty(Constants.BLOCK, id.toString());
        blockJson.add(Constants.PROPERTIES, properties);
        blockBelow = BlockReference.fromJson(blockJson);
        return this;
    }

    public AltarRecipeJS blockBelow(ResourceLocation id) {
        return blockBelow(id, new JsonObject());
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

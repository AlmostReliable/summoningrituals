package com.almostreliable.summoningrituals.compat.kubejs;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.recipe.component.RecipeSacrifices;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.EnumComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.UtilsJS;

public interface AltarRecipeSchema {

    RecipeKey<InputItem> CATALYST = ItemComponents.INPUT.key(Constants.CATALYST)
        .noBuilders();

    RecipeKey<RecipeOutputs> OUTPUTS = SummoningComponents.OUTPUTS.key(Constants.OUTPUTS)
        .noBuilders();

    RecipeKey<InputItem[]> INPUTS = ItemComponents.INPUT_ARRAY.key(Constants.INPUTS)
        .defaultOptional();

    RecipeKey<RecipeSacrifices> SACRIFICES = SummoningComponents.SACRIFICES.key(Constants.SACRIFICES)
        .optional(type -> new RecipeSacrifices())
        .noBuilders();

    RecipeKey<Integer> RECIPE_TIME = NumberComponent.ANY_INT.key(Constants.RECIPE_TIME)
        .preferred(UtilsJS.snakeCaseToCamelCase(Constants.RECIPE_TIME))
        .optional(100);

    RecipeKey<BlockReference> BLOCK_BELOW = SummoningComponents.BLOCK_REFERENCE.key(Constants.BLOCK_BELOW)
        .defaultOptional();

    RecipeKey<AltarRecipe.DAY_TIME> DAY_TIME = new EnumComponent<>(AltarRecipe.DAY_TIME.class)
        .key(Constants.DAY_TIME)
        .preferred(UtilsJS.snakeCaseToCamelCase(Constants.DAY_TIME))
        .optional(AltarRecipe.DAY_TIME.ANY);

    RecipeKey<AltarRecipe.WEATHER> WEATHER = new EnumComponent<>(AltarRecipe.WEATHER.class)
        .key(Constants.WEATHER)
        .preferred(UtilsJS.snakeCaseToCamelCase(Constants.WEATHER))
        .optional(AltarRecipe.WEATHER.ANY);

    RecipeSchema SCHEMA = new RecipeSchema(AltarRecipeJS.class, AltarRecipeJS::new, CATALYST,
        OUTPUTS, INPUTS, SACRIFICES, BLOCK_BELOW, DAY_TIME, WEATHER, RECIPE_TIME
    ).constructor((recipe, schemaType, keys, from) -> {
        recipe.setValue(CATALYST, from.getValue(recipe, CATALYST));
        // set default values on any new recipe
        recipe.setValue(OUTPUTS, new RecipeOutputs());
        recipe.setValue(SACRIFICES, new RecipeSacrifices());
    }, CATALYST);
}

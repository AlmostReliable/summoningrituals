package com.almostreliable.summoningrituals.compat.kubejs.recipe;

import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.EntityInputsComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.EntityOutputComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.ItemOutputComponent;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.input.EntityInputs;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;
import com.almostreliable.summoningrituals.recipe.output.ItemOutput;

import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedIngredientComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

import java.util.List;

public interface AltarRecipeSchema {

    RecipeKey<Ingredient> CATALYST = IngredientComponent.INGREDIENT
        .key("catalyst", ComponentRole.INPUT)
        .noFunctions();
    RecipeKey<List<ItemOutput>> ITEM_OUTPUTS = ItemOutputComponent.INSTANCE.asList()
        .key("item_outputs", ComponentRole.OUTPUT)
        .functionNames(List.of("itemOutputs"))
        .optional(List.of())
        .allowEmpty()
        .exclude();
    RecipeKey<List<EntityOutput>> ENTITY_OUTPUTS = EntityOutputComponent.INSTANCE.asList()
        .key("entity_outputs", ComponentRole.OUTPUT)
        .functionNames(List.of("entityOutputs"))
        .optional(List.of())
        .allowEmpty()
        .exclude();
    RecipeKey<List<SizedIngredient>> ITEM_INPUTS = SizedIngredientComponent.FLAT.asList()
        .key("item_inputs", ComponentRole.INPUT)
        .functionNames(List.of("itemInputs"))
        .optional(List.of())
        .allowEmpty()
        .exclude();
    RecipeKey<EntityInputs> ENTITY_INPUTS = EntityInputsComponent.INSTANCE
        .key("entity_inputs", ComponentRole.INPUT)
        .noFunctions()
        .optional(EntityInputs.EMPTY)
        .exclude();
    RecipeKey<Integer> TICKS = NumberComponent.INT
        .key("ticks", ComponentRole.OTHER)
        .optional(AltarRecipe.DEFAULT_TICKS)
        .exclude();

    RecipeSchema SCHEMA = new RecipeSchema(CATALYST, ITEM_OUTPUTS, ENTITY_OUTPUTS, ITEM_INPUTS, ENTITY_INPUTS, TICKS)
        .factory(AltarKubeRecipe.FACTORY);
}

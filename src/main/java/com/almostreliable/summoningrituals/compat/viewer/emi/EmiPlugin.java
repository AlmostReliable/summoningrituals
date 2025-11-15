package com.almostreliable.summoningrituals.compat.viewer.emi;

import com.almostreliable.summoningrituals.compat.viewer.emi.entity.EntityEmiStack;
import com.almostreliable.summoningrituals.compat.viewer.emi.entity.EntityEmiStackSerializer;
import com.almostreliable.summoningrituals.core.Registration;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;

@SuppressWarnings("ClassNameSameAsAncestorName")
@EmiEntrypoint
public class EmiPlugin implements dev.emi.emi.api.EmiPlugin {

    static final EmiStack ALTAR_STACK = EmiStack.of(Registration.ALTAR_BLOCK);
    static final EmiRecipeCategory ALTAR_CATEGORY = new AltarEmiCategory();

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(ALTAR_CATEGORY);
        registry.addWorkstation(ALTAR_CATEGORY, ALTAR_STACK);

        var recipes = registry.getRecipeManager().getAllRecipesFor(Registration.ALTAR_RECIPE_TYPE.get());
        for (var recipe : recipes) {
            registry.addRecipe(new AltarEmiRecipe(recipe));
        }
    }

    @Override
    public void initialize(EmiInitRegistry registry) {
        registry.addIngredientSerializer(EntityEmiStack.class, new EntityEmiStackSerializer());
    }
}

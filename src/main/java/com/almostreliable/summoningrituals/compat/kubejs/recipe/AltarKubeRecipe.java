package com.almostreliable.summoningrituals.compat.kubejs.recipe;

import com.almostreliable.summoningrituals.compat.kubejs.builder.ConditionsBuilder;
import com.almostreliable.summoningrituals.core.Registration;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;

import java.util.function.Function;

public class AltarKubeRecipe extends KubeRecipe {

    public static final KubeRecipeFactory FACTORY = new KubeRecipeFactory(
        Registration.ALTAR_RECIPE_TYPE.getId(),
        AltarKubeRecipe.class,
        AltarKubeRecipe::new
    );

    public AltarKubeRecipe conditions(Function<ConditionsBuilder, ConditionsBuilder> conditions) {
        setValue(AltarRecipeSchema.CONDITIONS, conditions.apply(new ConditionsBuilder(sourceLine)).build());
        return this;
    }
}

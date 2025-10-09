package com.almostreliable.summoningrituals.compat.kubejs.recipe;

import com.almostreliable.summoningrituals.compat.kubejs.builder.StartConditionsBuilder;
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

    public AltarKubeRecipe startConditions(Function<StartConditionsBuilder, StartConditionsBuilder> conditions) {
        setValue(AltarRecipeSchema.START_CONDITIONS, conditions.apply(new StartConditionsBuilder()).build());
        return this;
    }
}

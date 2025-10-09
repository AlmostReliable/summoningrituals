package com.almostreliable.summoningrituals.compat.kubejs.recipe;

import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.recipe.input.EntityInputs;

import net.minecraft.core.BlockPos;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;

import java.util.function.Function;

public class AltarKubeRecipe extends KubeRecipe {

    public static final KubeRecipeFactory FACTORY = new KubeRecipeFactory(
        Registration.ALTAR_RECIPE_TYPE.getId(),
        AltarKubeRecipe.class,
        AltarKubeRecipe::new
    );

    public AltarKubeRecipe entityInputs(BlockPos zone, Function<EntityInputs.Builder, EntityInputs.Builder> inputs) {
        var entityInputs = inputs.apply(new EntityInputs.Builder(zone));
        setValue(AltarRecipeSchema.ENTITY_INPUTS, entityInputs.build());
        return this;
    }

    public AltarKubeRecipe entityInputs(Function<EntityInputs.Builder, EntityInputs.Builder> inputs) {
        var entityInputs = inputs.apply(new EntityInputs.Builder());
        setValue(AltarRecipeSchema.ENTITY_INPUTS, entityInputs.build());
        return this;
    }
}

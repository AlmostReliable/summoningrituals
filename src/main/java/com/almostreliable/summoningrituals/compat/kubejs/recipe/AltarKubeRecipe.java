package com.almostreliable.summoningrituals.compat.kubejs.recipe;

import com.almostreliable.summoningrituals.compat.kubejs.builder.EntityInputsBuilder;
import com.almostreliable.summoningrituals.core.Registration;

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

    public AltarKubeRecipe entityInputs(BlockPos zone, Function<EntityInputsBuilder, EntityInputsBuilder> inputs) {
        var entityInputs = inputs.apply(new EntityInputsBuilder(zone));
        setValue(AltarRecipeSchema.ENTITY_INPUTS, entityInputs.build());
        return this;
    }

    public AltarKubeRecipe entityInputs(Function<EntityInputsBuilder, EntityInputsBuilder> inputs) {
        var entityInputs = inputs.apply(new EntityInputsBuilder());
        setValue(AltarRecipeSchema.ENTITY_INPUTS, entityInputs.build());
        return this;
    }
}

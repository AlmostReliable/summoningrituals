package com.almostreliable.summoningrituals.compat.kubejs.recipe;

import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningInputBinding;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.recipe.input.EntityInputs;

import net.minecraft.core.BlockPos;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;

import java.util.function.Consumer;

public class AltarKubeRecipe extends KubeRecipe {

    public static final KubeRecipeFactory FACTORY = new KubeRecipeFactory(
        Registration.ALTAR_RECIPE_TYPE.getId(),
        AltarKubeRecipe.class,
        AltarKubeRecipe::new
    );

    public AltarKubeRecipe entityInputs(BlockPos zone, Consumer<EntityInputs.Builder> builder) {
        builder.accept(SummoningInputBinding.entityInputs(zone));
        return this;
    }

    public AltarKubeRecipe entityInputs(Consumer<EntityInputs.Builder> builder) {
        builder.accept(SummoningInputBinding.entityInputs());
        return this;
    }
}

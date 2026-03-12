package com.almostreliable.summoningrituals.compat.viewer.emi;

import com.almostreliable.summoningrituals.core.Registration;

import net.minecraft.network.chat.Component;

import dev.emi.emi.api.recipe.EmiRecipeCategory;

public class AltarEmiCategory extends EmiRecipeCategory {

    AltarEmiCategory() {
        super(Registration.ALTAR_RECIPE_TYPE.getId(), EmiPlugin.ALTAR_STACK);
    }

    @Override
    public Component getName() {
        return Registration.ALTAR_BLOCK.get().getName();
    }
}

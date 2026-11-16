package com.almostreliable.summoningrituals.compat.viewer.jei;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.SummoningRitualsClient;
import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.jei.entity.EntityIngredientJeiHelper;
import com.almostreliable.summoningrituals.compat.viewer.jei.entity.EntityIngredientJeiRenderer;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.core.ModRegistration;

import net.minecraft.resources.Identifier;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {

    public static final IIngredientType<EntityIngredient> ENTITY_INGREDIENT = () -> EntityIngredient.class;

    @Override
    public Identifier getPluginUid() {
        return SummoningRituals.getRL(Constants.RECIPE_VIEWER);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new AltarJeiCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var recipes = SummoningRitualsClient.getRecipeMap().byType(ModRegistration.ALTAR_RECIPE_TYPE.get());
        registration.addRecipes(AltarJeiCategory.TYPE, List.copyOf(recipes));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(AltarJeiCategory.TYPE, ModRegistration.ALTAR_BLOCK, ModRegistration.INDESTRUCTIBLE_ALTAR_BLOCK);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        registration.register(
            ENTITY_INGREDIENT,
            List.of(),
            new EntityIngredientJeiHelper(),
            EntityIngredientJeiRenderer.BOOKMARK_RENDERER,
            EntityIngredient.CODEC
        );
    }
}

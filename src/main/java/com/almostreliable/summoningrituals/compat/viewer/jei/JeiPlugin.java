package com.almostreliable.summoningrituals.compat.viewer.jei;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.jei.entity.EntityIngredientHelper;
import com.almostreliable.summoningrituals.compat.viewer.jei.entity.EntityIngredientJeiRenderer;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.core.Registration;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

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
    public ResourceLocation getPluginUid() {
        return SummoningRituals.getRL(Constants.RECIPE_VIEWER);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new AltarJeiCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        registration.addRecipes(AltarJeiCategory.TYPE, level.getRecipeManager().getAllRecipesFor(Registration.ALTAR_RECIPE_TYPE.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(Registration.ALTAR_BLOCK.toStack(), AltarJeiCategory.TYPE);
        registration.addRecipeCatalyst(Registration.INDESTRUCTIBLE_ALTAR_BLOCK.toStack(), AltarJeiCategory.TYPE);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        registration.register(
            ENTITY_INGREDIENT,
            List.of(),
            new EntityIngredientHelper(),
            EntityIngredientJeiRenderer.BOOKMARK_RENDERER,
            EntityIngredient.CODEC
        );
    }
}

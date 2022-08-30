package com.almostreliable.summoningrituals.compat.jei;

import com.almostreliable.summoningrituals.Setup;
import com.almostreliable.summoningrituals.compat.jei.blockingredient.BlockHelper;
import com.almostreliable.summoningrituals.compat.jei.blockingredient.BlockRenderer;
import com.almostreliable.summoningrituals.util.GameUtils;
import com.almostreliable.summoningrituals.util.TextUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

@JeiPlugin
public class AlmostJEI implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return TextUtils.getRL("jei");
    }

    @Override
    public void registerIngredients(IModIngredientRegistration r) {
        r.register(AlmostTypes.BLOCK, List.of(), new BlockHelper(), new BlockRenderer(16));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration r) {
        var guiHelper = r.getJeiHelpers().getGuiHelper();
        r.addRecipeCategories(new AltarCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration r) {
        var rm = GameUtils.getRecipeManager(null);
        r.addRecipes(AltarCategory.TYPE, rm.getAllRecipesFor(Setup.ALTAR_RECIPE.type().get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r) {
        r.addRecipeCatalyst(new ItemStack(Setup.ALTAR_ITEM.get()), AltarCategory.TYPE);
    }

    public static final class AlmostTypes {
        public static final IIngredientType<BlockState> BLOCK = () -> BlockState.class;

        private AlmostTypes() {}
    }
}

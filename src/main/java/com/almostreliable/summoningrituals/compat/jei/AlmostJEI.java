package com.almostreliable.summoningrituals.compat.jei;

import com.almostreliable.summoningrituals.Setup;
import com.almostreliable.summoningrituals.compat.jei.ingredient.block.BlockReferenceHelper;
import com.almostreliable.summoningrituals.compat.jei.ingredient.block.BlockReferenceRenderer;
import com.almostreliable.summoningrituals.compat.jei.ingredient.mob.MobHelper;
import com.almostreliable.summoningrituals.compat.jei.ingredient.mob.MobIngredient;
import com.almostreliable.summoningrituals.compat.jei.ingredient.mob.MobRenderer;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
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

import java.util.List;

import static com.almostreliable.summoningrituals.Constants.JEI;

@JeiPlugin
public class AlmostJEI implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return TextUtils.getRL(JEI);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration r) {
        r.register(AlmostTypes.BLOCK_REFERENCE, List.of(), new BlockReferenceHelper(), new BlockReferenceRenderer(16));
        r.register(AlmostTypes.MOB, List.of(), new MobHelper(), new MobRenderer(16));
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
        r.addRecipeCatalyst(new ItemStack(Setup.INDESTRUCTIBLE_ALTAR_ITEM.get()), AltarCategory.TYPE);
    }

    public static final class AlmostTypes {
        public static final IIngredientType<BlockReference> BLOCK_REFERENCE = () -> BlockReference.class;
        public static final IIngredientType<MobIngredient> MOB = () -> MobIngredient.class;

        private AlmostTypes() {}
    }
}

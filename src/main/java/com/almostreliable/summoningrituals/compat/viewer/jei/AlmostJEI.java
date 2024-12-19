package com.almostreliable.summoningrituals.compat.viewer.jei;

import com.almostreliable.summoningrituals.Registration;
import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.block.BlockReferenceHelper;
import com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.block.JEIBlockReferenceRenderer;
import com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.mob.JEIMobRenderer;
import com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.mob.MobHelper;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.util.Utils;
import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.almostreliable.summoningrituals.Constants.RECIPE_VIEWER;

@REIPluginCompatIgnore
@JeiPlugin
public class AlmostJEI implements IModPlugin {

    public static final IIngredientType<EntityIngredient> MOB = () -> EntityIngredient.class;
    public static final IIngredientType<BlockReference> BLOCK_REFERENCE = () -> BlockReference.class;

    @Override
    public ResourceLocation getPluginUid() {
        return Utils.getRL(RECIPE_VIEWER);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration r) {
        r.register(MOB, List.of(), new MobHelper(), new JEIMobRenderer(16));
        r.register(BLOCK_REFERENCE, List.of(), new BlockReferenceHelper(), new JEIBlockReferenceRenderer(16));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration r) {
        var guiHelper = r.getJeiHelpers().getGuiHelper();
        r.addRecipeCategories(new AltarCategoryJEI(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration r) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        r.addRecipes(
            AltarCategoryJEI.TYPE,
            level.getRecipeManager().getAllRecipesFor(Registration.ALTAR_RECIPE.type().get())
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r) {
        r.addRecipeCatalyst(new ItemStack(Registration.ALTAR_ITEM.get()), AltarCategoryJEI.TYPE);
        r.addRecipeCatalyst(new ItemStack(Registration.INDESTRUCTIBLE_ALTAR_ITEM.get()), AltarCategoryJEI.TYPE);
    }
}

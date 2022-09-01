package com.almostreliable.summoningrituals.compat.jei.ingredient.entity;

import com.almostreliable.summoningrituals.compat.jei.AlmostJEI.AlmostTypes;
import com.almostreliable.summoningrituals.util.Bruhtils;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class EntityHelper implements IIngredientHelper<EntityIngredient> {

    @Override
    public IIngredientType<EntityIngredient> getIngredientType() {
        return AlmostTypes.ENTITY;
    }

    @Override
    public String getDisplayName(EntityIngredient ingredient) {
        return ingredient.getDisplayName().getString();
    }

    @Override
    public String getUniqueId(EntityIngredient ingredient, UidContext context) {
        return Bruhtils.getId(ingredient.getEntityType()).toString();
    }

    @SuppressWarnings("removal")
    @Override
    public String getModId(EntityIngredient ingredient) {
        return Bruhtils.getId(ingredient.getEntityType()).getNamespace();
    }

    @SuppressWarnings("removal")
    @Override
    public String getResourceId(EntityIngredient ingredient) {
        return Bruhtils.getId(ingredient.getEntityType()).getPath();
    }

    @Override
    public ResourceLocation getResourceLocation(EntityIngredient ingredient) {
        return Bruhtils.getId(ingredient.getEntityType());
    }

    @Override
    public EntityIngredient copyIngredient(EntityIngredient ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(@Nullable EntityIngredient ingredient) {
        if (ingredient == null) {
            return "Null entity";
        }
        return "Entity: " + Bruhtils.getId(ingredient.getEntityType());
    }
}

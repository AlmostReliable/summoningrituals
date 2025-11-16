package com.almostreliable.summoningrituals.compat.viewer.jei.entity;

import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.jei.JeiPlugin;

import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;

import org.jetbrains.annotations.Nullable;

public class EntityIngredientHelper implements IIngredientHelper<EntityIngredient> {

    @Override
    public IIngredientType<EntityIngredient> getIngredientType() {
        return JeiPlugin.ENTITY_INGREDIENT;
    }

    @Override
    public String getDisplayName(EntityIngredient entity) {
        return entity.getDisplayName().getString();
    }

    @SuppressWarnings("removal")
    @Override
    public String getUniqueId(EntityIngredient entity, UidContext context) {
        return entity.getId().toString();
    }

    @Override
    public ResourceLocation getResourceLocation(EntityIngredient entity) {
        return entity.getId();
    }

    @Override
    public EntityIngredient copyIngredient(EntityIngredient entity) {
        return entity.copy();
    }

    @Override
    public long getAmount(EntityIngredient entity) {
        return entity.getEntityInfo().count();
    }

    @Override
    public String getErrorInfo(@Nullable EntityIngredient entity) {
        if (entity == null) {
            return "Null entity";
        }
        return "Entity: " + getResourceLocation(entity);
    }
}

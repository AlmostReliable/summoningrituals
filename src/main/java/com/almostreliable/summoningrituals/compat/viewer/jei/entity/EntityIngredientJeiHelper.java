package com.almostreliable.summoningrituals.compat.viewer.jei.entity;

import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.jei.JeiPlugin;

import net.minecraft.resources.Identifier;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;

import org.jspecify.annotations.Nullable;

public class EntityIngredientJeiHelper implements IIngredientHelper<EntityIngredient> {

    @Override
    public IIngredientType<EntityIngredient> getIngredientType() {
        return JeiPlugin.ENTITY_INGREDIENT;
    }

    @Override
    public String getDisplayName(EntityIngredient entity) {
        return entity.getDisplayName().getString();
    }

    @Override
    public Object getUid(EntityIngredient entity, UidContext context) {
        return entity.getId();
    }

    @Override
    public Identifier getIdentifier(EntityIngredient entity) {
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

        return "Entity: " + getIdentifier(entity);
    }
}

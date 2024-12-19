package com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.mob;

import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.jei.AlmostJEI;
import com.almostreliable.summoningrituals.platform.Platform;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class MobHelper implements IIngredientHelper<EntityIngredient> {

    @Override
    public IIngredientType<EntityIngredient> getIngredientType() {
        return AlmostJEI.MOB;
    }

    @Override
    public String getDisplayName(EntityIngredient mob) {
        return mob.getDisplayName().getString();
    }

    @Override
    public String getUniqueId(EntityIngredient mob, UidContext context) {
        return Platform.getId(mob.getEntityType()).toString();
    }

    @Override
    public ResourceLocation getResourceLocation(EntityIngredient mob) {
        return Platform.getId(mob.getEntityType());
    }

    @Override
    public EntityIngredient copyIngredient(EntityIngredient mob) {
        return new EntityIngredient(mob.getEntityType(), mob.getCount(), mob.getTag());
    }

    @Override
    public String getErrorInfo(@Nullable EntityIngredient mob) {
        if (mob == null) {
            return "Null entity";
        }
        return "Entity: " + Platform.getId(mob.getEntityType());
    }
}

package com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.mob;

import com.almostreliable.summoningrituals.compat.viewer.common.MobIngredient;
import com.almostreliable.summoningrituals.compat.viewer.jei.AlmostJEI;
import com.almostreliable.summoningrituals.platform.Platform;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class MobHelper implements IIngredientHelper<MobIngredient> {

    @Override
    public IIngredientType<MobIngredient> getIngredientType() {
        return AlmostJEI.MOB;
    }

    @Override
    public String getDisplayName(MobIngredient mob) {
        return mob.getDisplayName().getString();
    }

    @Override
    public String getUniqueId(MobIngredient mob, UidContext context) {
        return Platform.getId(mob.getEntityType()).toString();
    }

    @Override
    public ResourceLocation getResourceLocation(MobIngredient mob) {
        return Platform.getId(mob.getEntityType());
    }

    @Override
    public MobIngredient copyIngredient(MobIngredient mob) {
        return new MobIngredient(mob.getEntityType(), mob.getCount(), mob.getTag());
    }

    @Override
    public String getErrorInfo(@Nullable MobIngredient mob) {
        if (mob == null) {
            return "Null entity";
        }
        return "Entity: " + Platform.getId(mob.getEntityType());
    }
}

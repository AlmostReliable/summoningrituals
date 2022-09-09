package com.almostreliable.summoningrituals.compat.jei.ingredient.mob;

import com.almostreliable.summoningrituals.compat.jei.AlmostJEI.AlmostTypes;
import com.almostreliable.summoningrituals.util.Bruhtils;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MobHelper implements IIngredientHelper<MobIngredient> {

    @Override
    public IIngredientType<MobIngredient> getIngredientType() {
        return AlmostTypes.MOB;
    }

    @Override
    public String getDisplayName(MobIngredient mob) {
        return mob.getDisplayName().getString();
    }

    @Override
    public String getUniqueId(MobIngredient mob, UidContext context) {
        return Bruhtils.getId(mob.getEntityType()).toString();
    }

    @SuppressWarnings("removal")
    @Override
    public String getModId(MobIngredient mob) {
        return Bruhtils.getId(mob.getEntityType()).getNamespace();
    }

    @SuppressWarnings("removal")
    @Override
    public String getResourceId(MobIngredient mob) {
        return Bruhtils.getId(mob.getEntityType()).getPath();
    }

    @Override
    public ResourceLocation getResourceLocation(MobIngredient mob) {
        return Bruhtils.getId(mob.getEntityType());
    }

    @Override
    public MobIngredient copyIngredient(MobIngredient mob) {
        return mob;
    }

    @Override
    public String getErrorInfo(@Nullable MobIngredient mob) {
        if (mob == null) {
            return "Null entity";
        }
        return "Entity: " + Bruhtils.getId(mob.getEntityType());
    }
}

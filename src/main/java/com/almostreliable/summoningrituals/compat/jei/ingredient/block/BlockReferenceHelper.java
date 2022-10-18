package com.almostreliable.summoningrituals.compat.jei.ingredient.block;

import com.almostreliable.summoningrituals.compat.jei.AlmostJEI.AlmostTypes;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.util.Bruhtils;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BlockReferenceHelper implements IIngredientHelper<BlockReference> {

    @Override
    public IIngredientType<BlockReference> getIngredientType() {
        return AlmostTypes.BLOCK_REFERENCE;
    }

    @Override
    public String getDisplayName(BlockReference blockReference) {
        var displayName = blockReference.getDisplayState().getBlock().getName();
        return displayName.getString();
    }

    @Override
    public String getUniqueId(BlockReference blockReference, UidContext context) {
        return Bruhtils.getId(blockReference.getDisplayState().getBlock()).toString();
    }

    @Override
    public ResourceLocation getResourceLocation(BlockReference blockReference) {
        return Bruhtils.getId(blockReference.getDisplayState().getBlock());
    }

    @Override
    public BlockReference copyIngredient(BlockReference blockReference) {
        return blockReference;
    }

    @Override
    public String getErrorInfo(@Nullable BlockReference blockReference) {
        if (blockReference == null) {
            return "Null block reference";
        }
        return "Block: " + blockReference.getDisplayState().getBlock().getName() +
            "\nState: " + blockReference.getDisplayState();
    }
}

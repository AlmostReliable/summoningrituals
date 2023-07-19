package com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.block;

import com.almostreliable.summoningrituals.compat.viewer.jei.AlmostJEI;
import com.almostreliable.summoningrituals.platform.Platform;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class BlockReferenceHelper implements IIngredientHelper<BlockReference> {

    @Override
    public IIngredientType<BlockReference> getIngredientType() {
        return AlmostJEI.BLOCK_REFERENCE;
    }

    @Override
    public String getDisplayName(BlockReference blockReference) {
        var displayName = blockReference.getDisplayState().getBlock().getName();
        return displayName.getString();
    }

    @Override
    public String getUniqueId(BlockReference blockReference, UidContext context) {
        return Platform.getId(blockReference.getDisplayState().getBlock()).toString();
    }

    @Override
    public ResourceLocation getResourceLocation(BlockReference blockReference) {
        return Platform.getId(blockReference.getDisplayState().getBlock());
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

package com.almostreliable.summoningrituals.compat.jei.ingredient.block;

import com.almostreliable.summoningrituals.compat.jei.AlmostJEI.AlmostTypes;
import com.almostreliable.summoningrituals.util.Bruhtils;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockBelowHelper implements IIngredientHelper<BlockState> {

    @Override
    public IIngredientType<BlockState> getIngredientType() {
        return AlmostTypes.BLOCK_BELOW;
    }

    @Override
    public String getDisplayName(BlockState blockBelow) {
        var displayName = blockBelow.getBlock().getName();
        return displayName.getString();
    }

    @Override
    public String getUniqueId(BlockState blockBelow, UidContext context) {
        return Bruhtils.getId(blockBelow.getBlock()).toString();
    }

    @SuppressWarnings("removal")
    @Override
    public String getModId(BlockState blockBelow) {
        return Bruhtils.getId(blockBelow.getBlock()).getNamespace();
    }

    @SuppressWarnings("removal")
    @Override
    public String getResourceId(BlockState blockBelow) {
        return Bruhtils.getId(blockBelow.getBlock()).getPath();
    }

    @Override
    public ResourceLocation getResourceLocation(BlockState blockBelow) {
        return Bruhtils.getId(blockBelow.getBlock());
    }

    @Override
    public BlockState copyIngredient(BlockState blockBelow) {
        return blockBelow;
    }

    @Override
    public String getErrorInfo(@Nullable BlockState blockBelow) {
        if (blockBelow == null) {
            return "Null block state";
        }
        return "Block: " + blockBelow.getBlock().getName() +
            "\nState: " + blockBelow;
    }
}

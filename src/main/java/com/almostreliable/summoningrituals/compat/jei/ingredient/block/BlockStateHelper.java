package com.almostreliable.summoningrituals.compat.jei.ingredient.block;

import com.almostreliable.summoningrituals.compat.jei.AlmostJEI.AlmostTypes;
import com.almostreliable.summoningrituals.util.Bruhtils;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockStateHelper implements IIngredientHelper<BlockState> {

    @Override
    public IIngredientType<BlockState> getIngredientType() {
        return AlmostTypes.BLOCK_STATE;
    }

    @Override
    public String getDisplayName(BlockState blockState) {
        var displayName = blockState.getBlock().getName();
        return displayName.getString();
    }

    @Override
    public String getUniqueId(BlockState blockState, UidContext context) {
        return Bruhtils.getId(blockState.getBlock()).toString();
    }

    @SuppressWarnings("removal")
    @Override
    public String getModId(BlockState blockState) {
        return Bruhtils.getId(blockState.getBlock()).getNamespace();
    }

    @SuppressWarnings("removal")
    @Override
    public String getResourceId(BlockState blockState) {
        return Bruhtils.getId(blockState.getBlock()).getPath();
    }

    @Override
    public ResourceLocation getResourceLocation(BlockState blockState) {
        return Bruhtils.getId(blockState.getBlock());
    }

    @Override
    public BlockState copyIngredient(BlockState blockState) {
        return blockState;
    }

    @Override
    public String getErrorInfo(@Nullable BlockState blockState) {
        if (blockState == null) {
            return "Null block state";
        }
        return "Block: " + blockState.getBlock().getName() +
            "\nState: " + blockState;
    }
}

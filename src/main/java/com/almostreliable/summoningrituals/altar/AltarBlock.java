package com.almostreliable.summoningrituals.altar;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class AltarBlock extends Block implements EntityBlock {

    static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public AltarBlock() {
        super(Properties.of(Material.STONE).strength(2.5f).sound(SoundType.STONE));
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var superState = super.getStateForPlacement(context);
        var state = superState == null ? defaultBlockState() : superState;
        return state.setValue(ACTIVE, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (entityLevel, entityState, entityType, entity) -> {
            if (entity instanceof AltarEntity altar) {
                altar.tick();
            }
        };
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }
}

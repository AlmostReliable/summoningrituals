package com.almostreliable.summoningrituals.altar;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class AltarBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final VoxelShape SHAPE = Stream.of(
        Block.box(3, 0, 3, 13, 2, 13),
        Block.box(5, 2, 5, 11, 9, 11),
        Block.box(2, 9, 2, 14, 13, 14)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public AltarBlock() {
        super(Properties.of(Material.STONE).strength(2.5f).sound(SoundType.STONE));
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var superState = super.getStateForPlacement(context);
        var state = superState == null ? defaultBlockState() : superState;
        return state.setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(ACTIVE, false);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player instanceof ServerPlayer && level.getBlockEntity(pos) instanceof AltarEntity altar) {
            altar.playerDestroy(player.isCreative());
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING).add(ACTIVE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(
        BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && hand == InteractionHand.MAIN_HAND &&
            level.getBlockEntity(pos) instanceof AltarEntity altar) {
            return altar.handleInteraction(serverPlayer, player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarEntity(pos, state);
    }
}

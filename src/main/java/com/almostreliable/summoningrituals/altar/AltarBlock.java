package com.almostreliable.summoningrituals.altar;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.util.MathUtils;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class AltarBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {

    static final BooleanProperty ACTIVE = BooleanProperty.create(Constants.ACTIVE);
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE = Stream.of(
        Block.box(3, 0, 3, 13, 2, 13),
        Block.box(5, 2, 5, 11, 9, 11),
        Block.box(2, 9, 2, 14, 13, 14)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public AltarBlock(Properties properties) {
        super(properties);
        registerDefaultState(
            defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false)
                .setValue(WATERLOGGED, false)
        );
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(
        BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (hand == InteractionHand.MAIN_HAND && level.getBlockEntity(pos) instanceof AltarBlockEntity altar) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.setMainHandItem(altar.handleInteraction(serverPlayer, serverPlayer.getMainHandItem()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(
        BlockState state, Direction direction, BlockState nState, LevelAccessor level, BlockPos pos, BlockPos nPos
    ) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, nState, level, pos, nPos);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level level, BlockState state, BlockEntityType<T> type
    ) {
        if (level.isClientSide) return null;
        return (entityLevel, entityState, entityType, entity) -> {
            if (entity instanceof AltarBlockEntity altar) {
                altar.tick();
            }
        };
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Vector3f[][] particlePos = MathUtils.getHorizontalVectors(
            new Vector3f(3.5f, 1.5f, 9.5f),
            new Vector3f(9.5f, 3.5f, 12.5f),
            new Vector3f(11.5f, 4.5f, 10.5f)
        );

        var x = pos.getX();
        var y = pos.getY() + 1;
        var z = pos.getZ();
        var vec = particlePos[state.getValue(FACING).ordinal() - 2];
        var active = state.getValue(ACTIVE);

        for (var i = 0; i < 3; i++) {
            if (active) {
                renderCandleActive(level, x, y, z, vec[i]);
            } else {
                renderCandleInactive(level, x, y, z, vec[i]);
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var superState = super.getStateForPlacement(context);
        var state = superState == null ? defaultBlockState() : superState;
        return state.setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(ACTIVE, false)
            .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player instanceof ServerPlayer && level.getBlockEntity(pos) instanceof AltarBlockEntity altar) {
            altar.playerDestroy(player.isCreative());
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING).add(ACTIVE).add(WATERLOGGED);
    }

    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    private void renderCandleActive(Level level, int x, int y, int z, Vector3f vec) {
        level.addParticle(
            ParticleTypes.SOUL,
            x + vec.x() / 16f,
            y + (vec.y() + 2) / 16f,
            z + vec.z() / 16f,
            0,
            0,
            0
        );
    }

    private void renderCandleInactive(Level level, int x, int y, int z, Vector3f vec) {
        level.addParticle(ParticleTypes.SMALL_FLAME, x + vec.x() / 16f, y + vec.y() / 16f, z + vec.z() / 16f, 0, 0, 0);
    }
}

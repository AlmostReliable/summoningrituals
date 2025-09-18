package com.almostreliable.summoningrituals.altar;

import com.almostreliable.summoningrituals.core.Constants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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

import org.joml.Vector3f;

import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

public class AltarBlock extends TickableEntityBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create(Constants.ACTIVE);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE = Stream.of(
        box(3, 0, 3, 13, 2, 13),
        box(5, 2, 5, 11, 9, 11),
        box(2, 9, 2, 14, 13, 14)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    private static final Map<Direction, Vector3f[]> CANDLE_POSITIONS = initCandlePositions();

    public AltarBlock(Properties properties) {
        super(properties);
        registerDefaultState(
            defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING).add(ACTIVE).add(WATERLOGGED);
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState updateShape(
        BlockState state, Direction direction, BlockState nState, LevelAccessor level, BlockPos pos, BlockPos nPos
    ) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, nState, level, pos, nPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(
        ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
        InteractionHand hand, BlockHitResult hitResult
    ) {
        if (hand == InteractionHand.MAIN_HAND && level.getBlockEntity(pos) instanceof AltarBlockEntity altar) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.setItemInHand(
                    InteractionHand.MAIN_HAND,
                    altar.handleInteraction(serverPlayer, stack, false)
                );
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        var facing = state.getValue(FACING);
        var active = state.getValue(ACTIVE);

        Vector3f[] candlePositions = CANDLE_POSITIONS.get(facing);
        BlockPos posAbove = pos.above();

        for (var i = 0; i < 3; i++) {
            renderCandleParticles(
                level,
                posAbove,
                candlePositions[i],
                active ? ParticleTypes.SOUL : ParticleTypes.SMALL_FLAME,
                active ? 3 : 0
            );
        }
    }

    private void renderCandleParticles(Level level, BlockPos pos, Vector3f candlePosition, ParticleOptions particleType, int yOffset) {
        level.addParticle(
            particleType,
            pos.getX() + candlePosition.x() / 16f,
            pos.getY() + (candlePosition.y() + yOffset) / 16f,
            pos.getZ() + candlePosition.z() / 16f,
            0,
            0,
            0
        );
    }

    private static Map<Direction, Vector3f[]> initCandlePositions() {
        var candlePositions = new EnumMap<Direction, Vector3f[]>(Direction.class);

        var northPositions = new Vector3f[]{
            new Vector3f(3.5f, 1.5f, 9.5f),
            new Vector3f(9.5f, 3.5f, 12.5f),
            new Vector3f(11.5f, 4.5f, 10.5f)
        };
        candlePositions.put(Direction.NORTH, northPositions);

        candlePositions.put(
            Direction.SOUTH, new Vector3f[]{
                opposite(northPositions[0]),
                opposite(northPositions[1]),
                opposite(northPositions[2])
            }
        );

        candlePositions.put(
            Direction.EAST, new Vector3f[]{
                neighbor(northPositions[0]),
                neighbor(northPositions[1]),
                neighbor(northPositions[2])
            }
        );

        candlePositions.put(
            Direction.WEST, new Vector3f[]{
                opposite(neighbor(northPositions[0])),
                opposite(neighbor(northPositions[1])),
                opposite(neighbor(northPositions[2]))
            }
        );

        return candlePositions;
    }

    private static Vector3f opposite(Vector3f v) {
        return new Vector3f(16f - v.x(), v.y(), 16f - v.z());
    }

    private static Vector3f neighbor(Vector3f v) {
        Vector3f o = opposite(v);
        return new Vector3f(o.z(), v.y(), v.x());
    }
}

package com.almostreliable.summoningrituals.altar;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.util.MathUtils;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import java.util.Random;
import java.util.stream.Stream;

public class AltarBlock extends Block implements EntityBlock {

    static final BooleanProperty ACTIVE = BooleanProperty.create(Constants.ACTIVE);
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Stream.of(
        Block.box(3, 0, 3, 13, 2, 13),
        Block.box(5, 2, 5, 11, 9, 11),
        Block.box(2, 9, 2, 14, 13, 14)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public AltarBlock() {
        super(Properties.of(Material.STONE).strength(2.5f).sound(SoundType.STONE));
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(
        BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && hand == InteractionHand.MAIN_HAND &&
            level.getBlockEntity(pos) instanceof AltarEntity altar) {
            var result = altar.handleInteraction(serverPlayer, player.getItemInHand(InteractionHand.MAIN_HAND));
            return ItemStack.matches(result, player.getItemInHand(InteractionHand.MAIN_HAND)) ?
                InteractionResult.PASS : InteractionResult.SUCCESS;
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level level, BlockState state, BlockEntityType<T> type
    ) {
        if (level.isClientSide) return null;
        return (entityLevel, entityState, entityType, entity) -> {
            if (entity instanceof AltarEntity altar) {
                altar.tick();
            }
        };
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
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

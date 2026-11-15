package com.almostreliable.summoningrituals.client.render;

import com.almostreliable.summoningrituals.altar.AltarBlock;
import com.almostreliable.summoningrituals.client.util.AlphaBufferSource;
import com.almostreliable.summoningrituals.compat.kubejs.builder.BlockPatternConditionBuilder;
import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.data.SummoningTags;
import com.almostreliable.summoningrituals.recipe.condition.pattern.BlockPatternCondition;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class BlockPatternRenderer {

    public static final BlockPatternRenderer INSTANCE = new BlockPatternRenderer();
    private static final Map<BlockState, BlockEntity> BLOCK_ENTITY_CACHE = new IdentityHashMap<>();
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();
    private static final float PREVIEW_ALPHA = 0.5f;
    private static final float SCALE_FACTOR = 0.75f;
    private static final int TAG_CYCLE_TICKS = 20;

    @Nullable
    private Task task;

    public void render(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (task == null) return;

        var mc = Minecraft.getInstance();
        var level = mc.level;
        var player = mc.player;
        if (level == null || player == null) return;

        if (task.altarPos.distSqr(player.blockPosition()) > Math.pow(BlockPatternConditionBuilder.MAX_PATTERN_RADIUS + 5, 2)) {
            clear();
            return;
        }

        var altarState = level.getBlockState(task.altarPos);
        if (!altarState.is(SummoningTags.ALTARS)) {
            clear();
            return;
        }

        var age = level.getGameTime() - task.createdAt;
        var ticksPerBlock = task.patternEntries.size() * Config.CLIENT.previewTicksPerBlock.getAsInt();
        var maxAge = Mth.clamp(
            ticksPerBlock,
            Config.CLIENT.previewTicksMin.getAsInt(),
            Config.CLIENT.previewTicksMax.getAsInt()
        );
        if (age >= maxAge) {
            clear();
            return;
        }

        var alphaBuffer = new AlphaBufferSource(submitNodeCollector, PREVIEW_ALPHA);
        renderBlocks(mc, poseStack, alphaBuffer, camera, age / TAG_CYCLE_TICKS);
    }

    private void renderBlocks(Minecraft mc, PoseStack poseStack, AlphaBufferSource alphaBuffer, CameraRenderState camera, long cycleStep) {
        assert mc.level != null;
        assert task != null;

        var checkBlockState = Config.CLIENT.previewBlockStateAware.getAsBoolean();
        //noinspection ComplexTypesVar
        List<BlockPos> invalidBlocks = new ArrayList<>();
        if (checkBlockState) {
            invalidBlocks = task.blockPredicate.get();
        }

        var correctBlocks = 0;
        for (var entry : task.patternEntries.entrySet()) {
            var blockStates = entry.getValue();
            if (blockStates.isEmpty()) continue;

            var worldPos = task.altarPos.offset(entry.getKey());
            var blockState = mc.level.getBlockState(worldPos);

            if (checkBlockState) {
                if (!invalidBlocks.contains(worldPos)) {
                    correctBlocks++;
                    continue;
                }
            } else {
                if (!isWrongBlock(blockState, blockStates)) {
                    correctBlocks++;
                    continue;
                }
            }

            BlockHighlightRenderer.highlightWrongPositionOnce(worldPos);

            var translation = Vec3.atLowerCornerOf(worldPos).subtract(camera.pos);
            var blockStateToRender = blockStates.get((int) (cycleStep % blockStates.size()));

            poseStack.pushPose();
            {
                poseStack.translate(translation.x, translation.y, translation.z);

                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
                poseStack.translate(-0.5, -0.5, -0.5);

                renderBlock(mc, poseStack, alphaBuffer, camera, worldPos, blockStateToRender);
            }
            poseStack.popPose();
        }

        updatePlayerFeedback(mc, task.patternEntries.size(), correctBlocks);
    }

    private static void renderBlock(
        Minecraft mc, PoseStack poseStack, AlphaBufferSource alphaBuffer, CameraRenderState camera, BlockPos pos, BlockState blockState
    ) {
        var modelRenderState = new BlockModelRenderState();
        mc.getBlockModelResolver().update(modelRenderState, blockState, DISPLAY_CONTEXT);

        alphaBuffer.resetSubmitted();
        modelRenderState.submitMultiLayer(poseStack, alphaBuffer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        if (alphaBuffer.hasSubmitted()) return;

        if (blockState.getBlock() instanceof BaseEntityBlock entityBlock) {
            renderBlockEntity(mc, poseStack, alphaBuffer, camera, pos, entityBlock, blockState);
        }
    }

    private static void renderBlockEntity(
        Minecraft mc, PoseStack poseStack, AlphaBufferSource alphaBuffer, CameraRenderState camera, BlockPos pos,
        BaseEntityBlock entityBlock, BlockState blockState
    ) {
        assert mc.level != null;

        var blockEntity = BLOCK_ENTITY_CACHE.get(blockState);
        if (blockEntity == null) {
            blockEntity = entityBlock.newBlockEntity(pos, blockState);
            BLOCK_ENTITY_CACHE.put(blockState, blockEntity);
            if (blockEntity == null) return;
            blockEntity.setLevel(mc.level);
        }

        var dispatcher = mc.getBlockEntityRenderDispatcher();
        var partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        BlockEntityRenderState renderState = dispatcher.tryExtractRenderState(blockEntity, partialTick, null, null);
        if (renderState == null) return;

        renderState.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        dispatcher.submit(renderState, poseStack, alphaBuffer, camera);
    }

    private void updatePlayerFeedback(Minecraft mc, int totalBlocks, int correctBlocks) {
        var player = mc.player;
        if (player == null) return;

        if (correctBlocks == totalBlocks) {
            var message = SummoningLang.PREVIEW_SUCCESS.get().withStyle(ChatFormatting.DARK_GREEN);
            player.sendOverlayMessage(message);
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1));
            clear();
            return;
        }

        var message = SummoningLang.PREVIEW_IN_PROGRESS.get()
            .append(" (" + correctBlocks)
            .append("/")
            .append(totalBlocks + ")")
            .withStyle(ChatFormatting.YELLOW);
        player.sendOverlayMessage(message);
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static void scheduleTask(BlockPatternCondition blockPattern) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var level = mc.level;
        if (player == null || level == null) return;

        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
        player.closeContainer();

        var playerPos = player.position();
        var searchRadius = Config.CLIENT.altarSearchRadius.getAsInt();
        var searchArea = AABB.ofSize(playerPos, searchRadius, searchRadius, searchRadius);
        var altarSearchEntries = BlockPos.betweenClosedStream(searchArea)
            .map(pos -> new AltarSearchEntry(pos.immutable(), level.getBlockState(pos)))
            .filter(entry -> level.getBlockState(entry.pos).is(SummoningTags.ALTARS))
            .sorted(Comparator.comparingDouble(entry -> entry.pos.distToCenterSqr(playerPos)))
            .toList();
        var altarEntry = altarSearchEntries.stream()
            .filter(entry -> !entry.state.getValue(AltarBlock.ACTIVE))
            .findFirst()
            .orElseGet(() -> altarSearchEntries.isEmpty() ? null : altarSearchEntries.getFirst());

        if (altarEntry == null) {
            player.sendOverlayMessage(SummoningLang.PREVIEW_NO_ALTAR.get().withStyle(ChatFormatting.RED));
            return;
        }

        var altarPos = altarEntry.pos;
        var altarState = altarEntry.state;
        var altarFacing = altarState.getValue(AltarBlock.FACING);
        Supplier<List<BlockPos>> blockPredicate = () -> blockPattern.test(level, altarPos, altarState);
        INSTANCE.task = new Task(level.getGameTime(), level, altarPos, blockPattern.getTransformedEntries(altarFacing), blockPredicate);
    }

    public static void clear() {
        INSTANCE.task = null;
        BLOCK_ENTITY_CACHE.clear();
    }

    private static boolean isWrongBlock(BlockState blockState, List<BlockState> validBlockStates) {
        if (blockState.isAir()) return true;

        for (var expectedState : validBlockStates) {
            if (blockState.is(expectedState.getBlock())) return false;
        }

        return true;
    }

    private record AltarSearchEntry(BlockPos pos, BlockState state) {}

    private record Task(
        long createdAt, Level level, BlockPos altarPos, Map<BlockPos, List<BlockState>> patternEntries,
        Supplier<List<BlockPos>> blockPredicate
    ) {}
}

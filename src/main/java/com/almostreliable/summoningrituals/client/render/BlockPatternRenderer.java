package com.almostreliable.summoningrituals.client.render;

import com.almostreliable.summoningrituals.SummoningRituals;
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
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
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
    private static final ContextKey<List<PreviewRenderState>> PREVIEW_RENDER_STATES = new ContextKey<>(
        SummoningRituals.getRL("pattern_preview")
    );
    private static final Map<BlockState, BlockEntity> BLOCK_ENTITY_CACHE = new IdentityHashMap<>();
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();
    private static final float PREVIEW_ALPHA = 0.5f;
    private static final float SCALE_FACTOR = 0.75f;
    private static final int TAG_CYCLE_TICKS = 20;

    @Nullable
    private Task task;
    private List<PreviewBlock> previewBlocks = List.of();

    public void tick() {
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

        updatePreviewBlocks(mc, level, task, age / TAG_CYCLE_TICKS);
    }

    public void extractRenderState(LevelRenderState levelRenderState, float partialTick) {
        if (previewBlocks.isEmpty()) return;

        var mc = Minecraft.getInstance();
        var renderStates = new ArrayList<PreviewRenderState>(previewBlocks.size());

        for (var previewBlock : previewBlocks) {
            var modelRenderState = new BlockModelRenderState();
            mc.getBlockModelResolver().update(modelRenderState, previewBlock.state, DISPLAY_CONTEXT);
            var blockEntityRenderState = extractBlockEntityRenderState(mc, previewBlock, partialTick);
            var renderState = new PreviewRenderState(previewBlock.pos, modelRenderState, blockEntityRenderState);
            renderStates.add(renderState);
        }

        levelRenderState.setRenderData(PREVIEW_RENDER_STATES, renderStates);
    }

    public static void submit(LevelRenderState levelRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        var renderStates = levelRenderState.getRenderData(PREVIEW_RENDER_STATES);
        if (renderStates == null) return;

        var camera = levelRenderState.cameraRenderState;
        var alphaBuffer = new AlphaBufferSource(submitNodeCollector, PREVIEW_ALPHA);

        for (var renderState : renderStates) {
            var translation = Vec3.atLowerCornerOf(renderState.pos).subtract(camera.pos);

            poseStack.pushPose();
            {
                poseStack.translate(translation.x, translation.y, translation.z);

                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
                poseStack.translate(-0.5, -0.5, -0.5);

                submitBlock(poseStack, alphaBuffer, camera, renderState);
            }
            poseStack.popPose();
        }
    }

    private void updatePreviewBlocks(Minecraft mc, Level level, Task task, long cycleStep) {
        var checkBlockState = Config.CLIENT.previewBlockStateAware.getAsBoolean();
        var invalidBlocks = checkBlockState ? task.blockPredicate.get() : List.<BlockPos> of();

        var wrongBlocks = new ArrayList<PreviewBlock>();
        var correctBlocks = 0;
        for (var entry : task.patternEntries.entrySet()) {
            var blockStates = entry.getValue();
            if (blockStates.isEmpty()) continue;

            var worldPos = task.altarPos.offset(entry.getKey());
            var blockState = level.getBlockState(worldPos);

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

            BlockHighlights.highlightWrongPosition(worldPos);

            var blockStateToRender = blockStates.get((int) (cycleStep % blockStates.size()));
            var previewBlock = new PreviewBlock(worldPos, blockStateToRender);
            wrongBlocks.add(previewBlock);
        }

        previewBlocks = wrongBlocks;
        updatePlayerFeedback(mc, task.patternEntries.size(), correctBlocks);
    }

    @Nullable
    private static BlockEntityRenderState extractBlockEntityRenderState(Minecraft mc, PreviewBlock previewBlock, float partialTick) {
        var level = mc.level;
        if (level == null || !(previewBlock.state.getBlock() instanceof BaseEntityBlock entityBlock)) return null;

        var blockEntity = BLOCK_ENTITY_CACHE.get(previewBlock.state);
        if (blockEntity == null) {
            blockEntity = entityBlock.newBlockEntity(previewBlock.pos, previewBlock.state);
            if (blockEntity == null) return null;

            blockEntity.setLevel(level);
            BLOCK_ENTITY_CACHE.put(previewBlock.state, blockEntity);
        }

        var renderState = mc.getBlockEntityRenderDispatcher().tryExtractRenderState(blockEntity, partialTick, null, null);
        if (renderState == null) return null;

        renderState.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        return renderState;
    }

    private static void submitBlock(
        PoseStack poseStack,
        AlphaBufferSource alphaBuffer,
        CameraRenderState camera,
        PreviewRenderState renderState
    ) {
        alphaBuffer.resetSubmitted();
        renderState.model.submitMultiLayer(poseStack, alphaBuffer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        if (alphaBuffer.hasSubmitted() || renderState.blockEntity == null) return;

        Minecraft.getInstance().getBlockEntityRenderDispatcher().submit(renderState.blockEntity, poseStack, alphaBuffer, camera);
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
        INSTANCE.previewBlocks = List.of();
    }

    public static void clear() {
        INSTANCE.task = null;
        INSTANCE.previewBlocks = List.of();
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

    private record PreviewBlock(BlockPos pos, BlockState state) {}

    private record PreviewRenderState(BlockPos pos, BlockModelRenderState model, @Nullable BlockEntityRenderState blockEntity) {}

    private record Task(
        long createdAt, Level level, BlockPos altarPos, Map<BlockPos, List<BlockState>> patternEntries,
        Supplier<List<BlockPos>> blockPredicate
    ) {}
}

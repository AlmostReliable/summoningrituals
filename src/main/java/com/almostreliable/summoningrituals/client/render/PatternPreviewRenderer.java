package com.almostreliable.summoningrituals.client.render;

import com.almostreliable.summoningrituals.altar.AltarBlock;
import com.almostreliable.summoningrituals.client.util.AlphaBufferSource;
import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.recipe.condition.custom.BlockPatternCheck;
import com.almostreliable.summoningrituals.recipe.condition.custom.BlockPatternCheck.ClientPatternEntry;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public class PatternPreviewRenderer {

    public static final PatternPreviewRenderer INSTANCE = new PatternPreviewRenderer();
    private static final Map<BlockState, BlockEntity> BLOCK_ENTITY_CACHE = new IdentityHashMap<>();
    private static final float PREVIEW_ALPHA = 0.5f;
    private static final float SCALE_FACTOR = 0.75f;
    private static final int TAG_CYCLE_TICKS = 20;

    @Nullable
    private Task task;

    public void render(PoseStack poseStack, Camera camera) {
        if (task == null) return;

        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null) return;

        var age = level.getGameTime() - task.createdAt;
        var ticksPerBlock = task.pattern.size() * Config.CLIENT.patternPreviewTicksPerBlock.getAsInt();
        var maxAge = Mth.clamp(
            ticksPerBlock,
            Config.CLIENT.patternPreviewTicksMin.getAsInt(),
            Config.CLIENT.patternPreviewTicksMax.getAsInt()
        );
        if (age >= maxAge) {
            clear();
            return;
        }

        try {
            RenderSystem.disableCull();
            ModelBlockRenderer.enableCaching();
            var alphaBuffer = AlphaBufferSource.INSTANCE;
            alphaBuffer.setAlpha(PREVIEW_ALPHA);
            renderBlocks(mc, poseStack, camera, alphaBuffer, age / TAG_CYCLE_TICKS);
            alphaBuffer.endBatch();
        } finally {
            RenderSystem.enableCull();
            ModelBlockRenderer.clearCache();
        }
    }

    private void renderBlocks(Minecraft mc, PoseStack poseStack, Camera camera, AlphaBufferSource alphaBuffer, long cycleStep) {
        assert mc.level != null;
        assert task != null;

        var correctBlocks = 0;

        for (var entry : task.pattern) {
            var blockStates = entry.blocks();
            if (blockStates.isEmpty()) continue;

            var worldPos = task.altarPos.offset(entry.offset());
            var blockState = mc.level.getBlockState(worldPos);
            if (!isWrongBlock(blockState, blockStates)) {
                correctBlocks++;
                continue;
            }

            WrongBlockHighlightRenderer.renderWrongBlockOutline(
                poseStack,
                camera,
                mc.renderBuffers().bufferSource(),
                worldPos
            );

            var translation = Vec3.atLowerCornerOf(worldPos).subtract(camera.getPosition());
            var blockStateToRender = blockStates.get((int) (cycleStep % blockStates.size()));

            poseStack.pushPose();
            {
                poseStack.translate(translation.x, translation.y, translation.z);

                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
                poseStack.translate(-0.5, -0.5, -0.5);

                renderBlock(mc, poseStack, alphaBuffer, worldPos, blockStateToRender);
            }
            poseStack.popPose();
        }

        updatePlayerFeedback(mc, task.pattern.size(), correctBlocks);
    }

    private static void renderBlock(Minecraft mc, PoseStack poseStack, AlphaBufferSource alphaBuffer, BlockPos pos, BlockState blockState) {
        if (blockState.getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED &&
            blockState.getBlock() instanceof BaseEntityBlock entityBlock) {
            renderBlockEntity(mc, poseStack, alphaBuffer, pos, entityBlock, blockState);
            return;
        }

        //noinspection DataFlowIssue
        mc.getBlockRenderer().renderSingleBlock(
            blockState,
            poseStack,
            alphaBuffer,
            LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY,
            ModelData.EMPTY,
            null
        );
    }

    private static void renderBlockEntity(
        Minecraft mc, PoseStack poseStack, AlphaBufferSource alphaBuffer, BlockPos pos, BaseEntityBlock entityBlock, BlockState blockState
    ) {
        assert mc.level != null;

        var blockEntity = BLOCK_ENTITY_CACHE.get(blockState);
        if (blockEntity == null) {
            blockEntity = entityBlock.newBlockEntity(pos, blockState);
            BLOCK_ENTITY_CACHE.put(blockState, blockEntity);
            if (blockEntity == null) return;
            blockEntity.setLevel(mc.level);
        }

        mc.getBlockEntityRenderDispatcher()
            .renderItem(blockEntity, poseStack, alphaBuffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    private void updatePlayerFeedback(Minecraft mc, int totalBlocks, int correctBlocks) {
        var player = mc.player;
        if (player == null) return;

        if (correctBlocks == totalBlocks) {
            var message = SummoningLang.PREVIEW_SUCCESS.get().withStyle(ChatFormatting.DARK_GREEN);
            player.displayClientMessage(message, true);
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1));
            clear();
            return;
        }

        var message = SummoningLang.PREVIEW_IN_PROGRESS.get()
            .append(" (" + correctBlocks)
            .append("/")
            .append(totalBlocks + ")")
            .withStyle(ChatFormatting.YELLOW);
        player.displayClientMessage(message, true);
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static void scheduleTask(BlockPatternCheck blockPatternCheck) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var level = mc.level;
        if (player == null || level == null) return;

        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
        player.closeContainer();

        var playerPos = player.position();
        var searchRadius = Config.CLIENT.patternPreviewSearchRadius.getAsInt();
        var searchArea = AABB.ofSize(playerPos, searchRadius, searchRadius, searchRadius);
        var altarSearchEntries = BlockPos.betweenClosedStream(searchArea)
            .map(pos -> new AltarSearchEntry(pos.immutable(), level.getBlockState(pos)))
            .filter(entry -> level.getBlockState(entry.pos).is(Registration.ALTAR_BLOCK))
            .sorted(Comparator.comparingDouble(entry -> entry.pos.distToCenterSqr(playerPos)))
            .toList();
        var altarPos = altarSearchEntries.stream()
            .filter(entry -> !entry.state.getValue(AltarBlock.ACTIVE))
            .map(AltarSearchEntry::pos)
            .findFirst()
            .orElseGet(() -> altarSearchEntries.isEmpty() ? null : altarSearchEntries.getFirst().pos);

        if (altarPos == null) {
            player.displayClientMessage(SummoningLang.PREVIEW_NO_ALTAR.get().withStyle(ChatFormatting.RED), true);
            return;
        }

        INSTANCE.task = new Task(level.getGameTime(), level, altarPos, blockPatternCheck.getRenderPattern());
    }

    public static void clear() {
        INSTANCE.task = null;
        BLOCK_ENTITY_CACHE.clear();
    }

    private static boolean isWrongBlock(BlockState blockState, List<BlockState> validBlockStates) {
        if (blockState.isAir()) return true;

        for (var expectedState : validBlockStates) {
            if (blockState.is(expectedState.getBlock())) {
                return false;
            }
        }

        return true;
    }

    private record AltarSearchEntry(BlockPos pos, BlockState state) {}

    private record Task(long createdAt, Level level, BlockPos altarPos, List<ClientPatternEntry> pattern) {}

    private static final class WrongBlockHighlightRenderer {

        private static final RenderType XRAY_LINES = RenderType.create(
            "summoningrituals_xray_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(2.0)))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .createCompositeState(false)
        );

        private static void renderWrongBlockOutline(PoseStack poseStack, Camera camera, MultiBufferSource buffer, BlockPos pos) {
            var cam = camera.getPosition();
            var box = new AABB(pos).inflate(0.002); // avoid z-fighting

            poseStack.pushPose();
            poseStack.translate(-cam.x, -cam.y, -cam.z);
            {
                LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(XRAY_LINES), box, 1, 0, 0, 1);
            }
            poseStack.popPose();
        }
    }
}

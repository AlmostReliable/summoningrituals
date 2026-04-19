package com.almostreliable.summoningrituals.client.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.OptionalDouble;

public final class BlockHighlightRenderer {

    public static final BlockHighlightRenderer INSTANCE = new BlockHighlightRenderer();
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
    private static final int HIGHLIGHT_DURATION = 100;

    private final Deque<WrongBlockTask> wrongBlockTasks = new ArrayDeque<>();
    private @Nullable AreaTask areaTask;

    private BlockHighlightRenderer() {}

    public static void highlightWrongPositions(long gameTime, Iterable<BlockPos> positions) {
        INSTANCE.wrongBlockTasks.add(new WrongBlockTask(gameTime, positions));
    }

    public static void highlightWrongPositionOnce(BlockPos pos) {
        INSTANCE.wrongBlockTasks.add(new WrongBlockTask(0, List.of(pos)));
    }

    public static void highlightArea(long gameTime, AABB area) {
        INSTANCE.areaTask = new AreaTask(gameTime, area);
    }

    public static void clearAreaTask() {
        INSTANCE.areaTask = null;
    }

    public static void clear() {
        INSTANCE.wrongBlockTasks.clear();
        INSTANCE.areaTask = null;
    }

    public void render(PoseStack poseStack, Camera camera) {
        if (wrongBlockTasks.isEmpty() && areaTask == null) return;

        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null) return;

        var gameTime = level.getGameTime();
        var bufferSource = mc.renderBuffers().bufferSource();

        if (areaTask != null) {
            renderOutline(poseStack, camera, bufferSource, areaTask.area, 0, 1, 1);

            if (gameTime - areaTask.createdAt > HIGHLIGHT_DURATION) {
                areaTask = null;
            }
        }

        var it = wrongBlockTasks.iterator();
        while (it.hasNext()) {
            var task = it.next();

            if (task.createdAt != 0 && gameTime - task.createdAt > HIGHLIGHT_DURATION) {
                it.remove();
                continue;
            }

            for (var pos : task.positions) {
                renderOutline(poseStack, camera, bufferSource, new AABB(pos), 1, 0, 0);
            }

            if (task.createdAt == 0) {
                it.remove();
            }
        }
    }

    private void renderOutline(
        PoseStack poseStack, Camera camera, MultiBufferSource buffer, AABB area, float red, float green, float blue) {
        var cameraPos = camera.getPosition();
        var inflatedArea = area.inflate(0.002); // avoid z-fighting

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        {
            LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(XRAY_LINES), inflatedArea, red, green, blue, 1);
        }
        poseStack.popPose();
    }

    private record WrongBlockTask(long createdAt, Iterable<BlockPos> positions) {}

    private record AreaTask(long createdAt, AABB area) {}
}

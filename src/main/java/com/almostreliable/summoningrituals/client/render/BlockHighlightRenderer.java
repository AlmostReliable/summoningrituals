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

    private final Deque<Task> tasks = new ArrayDeque<>();

    private BlockHighlightRenderer() {}

    public static void scheduleTask(long gameTime, Iterable<BlockPos> positions) {
        INSTANCE.tasks.add(new Task(gameTime, HIGHLIGHT_DURATION, positions));
    }

    public static void renderOutline(BlockPos pos) {
        INSTANCE.tasks.add(new Task(0, 0, List.of(pos)));
    }

    public static void clear() {
        INSTANCE.tasks.clear();
    }

    public void render(PoseStack poseStack, Camera camera) {
        if (tasks.isEmpty()) return;

        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null) return;

        var gameTime = level.getGameTime();
        var bufferSource = mc.renderBuffers().bufferSource();

        var it = tasks.iterator();
        while (it.hasNext()) {
            var task = it.next();

            if (task.duration != 0 && gameTime - task.createdAt > task.duration) {
                it.remove();
                continue;
            }

            for (var pos : task.positions) {
                renderOutline(poseStack, camera, bufferSource, pos);
            }

            if (task.duration == 0) {
                it.remove();
            }
        }
    }

    private void renderOutline(PoseStack poseStack, Camera camera, MultiBufferSource buffer, BlockPos pos) {
        var cameraPos = camera.getPosition();
        var blockBox = new AABB(pos).inflate(0.002); // avoid z-fighting

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        {
            LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(XRAY_LINES), blockBox, 1, 0, 0, 1);
        }
        poseStack.popPose();
    }

    private record Task(long createdAt, int duration, Iterable<BlockPos> positions) {}
}

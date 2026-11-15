package com.almostreliable.summoningrituals.client.render;

import com.almostreliable.summoningrituals.SummoningRituals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class BlockHighlightRenderer {

    public static final BlockHighlightRenderer INSTANCE = new BlockHighlightRenderer();
    public static final RenderPipeline XRAY_LINES_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
        .withLocation(SummoningRituals.getRL("pipeline/xray_lines"))
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .build();
    private static final RenderType XRAY_LINES = RenderType.create(
        "summoningrituals_xray_lines",
        RenderSetup.builder(XRAY_LINES_PIPELINE).createRenderSetup()
    );
    private static final float LINE_WIDTH = 2f;
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

    public void render(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 cameraPos) {
        if (wrongBlockTasks.isEmpty() && areaTask == null) return;

        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null) return;

        var gameTime = level.getGameTime();

        if (areaTask != null) {
            renderOutline(poseStack, submitNodeCollector, cameraPos, areaTask.area, 0, 1, 1);

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
                renderOutline(poseStack, submitNodeCollector, cameraPos, new AABB(pos), 1, 0, 0);
            }

            if (task.createdAt == 0) {
                it.remove();
            }
        }
    }

    private void renderOutline(
        PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 cameraPos, AABB area, float red, float green, float blue
    ) {
        var inflatedArea = area.inflate(0.002); // avoid z-fighting
        var color = ARGB.colorFromFloat(1, red, green, blue);

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        {
            submitNodeCollector.submitCustomGeometry(poseStack, XRAY_LINES, (pose, buffer) -> Shapes.create(inflatedArea).forAllEdges(
                (x1, y1, z1, x2, y2, z2) -> {
                    var normal = new Vector3f((float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1)).normalize();
                    buffer.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(color).setNormal(pose, normal).setLineWidth(LINE_WIDTH);
                    buffer.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(color).setNormal(pose, normal).setLineWidth(LINE_WIDTH);
                }
            ));
        }
        poseStack.popPose();
    }

    private record WrongBlockTask(long createdAt, Iterable<BlockPos> positions) {}

    private record AreaTask(long createdAt, AABB area) {}
}

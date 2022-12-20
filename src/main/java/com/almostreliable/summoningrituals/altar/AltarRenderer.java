package com.almostreliable.summoningrituals.altar;

import com.almostreliable.summoningrituals.util.MathUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;

public class AltarRenderer implements BlockEntityRenderer<AltarBlockEntity> {

    private static final int MAX_RENDER_DISTANCE = 32;
    private static final int MAX_ITEM_HEIGHT = 2;
    private static final int MAX_RESET = 60;
    private static final float MAX_PROGRESS_HEIGHT = 2.5f;
    private static final float HALF = .5f;
    private static final float ITEM_OFFSET = 1.5f;

    private final Minecraft mc;
    private final ItemRenderer itemRenderer;

    private float resetTimer;
    private double oldCircleOffset;

    public AltarRenderer(Context ignoredContext) {
        mc = Minecraft.instance;
        itemRenderer = mc.itemRenderer;
    }

    @Override
    public void render(
        AltarBlockEntity entity, float partial, PoseStack stack, MultiBufferSource buffer, int light, int overlay
    ) {
        if (mc.player == null || entity.level == null ||
            entity.blockPos.distSqr(mc.player.blockPosition()) > Math.pow(MAX_RENDER_DISTANCE, 2)) {
            return;
        }

        stack.pushPose();
        {
            stack.translate(HALF, 0.8f, HALF);
            stack.scale(HALF, HALF, HALF);

            var lightAbove = LevelRenderer.getLightColor(entity.level, entity.blockPos.above());
            var altarPos = MathUtils.shiftToCenter(MathUtils.vectorFromPos(entity.blockPos));
            var playerPos = mc.player.position();
            var playerAngle = Math.toDegrees(Math.atan2(altarPos.x - playerPos.x, playerPos.z - altarPos.z)) + 180;

            var progress = entity.progress;
            var processTime = entity.processTime;

            stack.translate(0, MAX_PROGRESS_HEIGHT * MathUtils.modifier(progress, processTime, 0), 0);

            if (!entity.inventory.catalyst.isEmpty) {
                stack.pushPose();
                {
                    stack.translate(0, 1 - 0.75f * MathUtils.modifier(progress, processTime, 0), 0);
                    stack.scale(0.75f, 0.75f, 0.75f);
                    stack.mulPose(Vector3f.YN.rotationDegrees((float) playerAngle));
                    itemRenderer
                        .renderStatic(
                            entity.inventory.catalyst,
                            TransformType.FIXED,
                            lightAbove,
                            overlay,
                            stack,
                            buffer,
                            (int) entity.blockPos.asLong()
                        );
                }
                stack.popPose();
            }

            var axisRotation = MathUtils.singleRotation(entity.level.gameTime);
            var scale = 1 - MathUtils.modifier(progress, processTime, 0);

            if (progress == 0 && resetTimer > 0) {
                scale = 1 - MathUtils.modifier(resetTimer, MAX_RESET, 0);
                resetTimer = Math.max(0, resetTimer - partial);
            }

            stack.scale(scale, scale, scale);

            var inputs = entity.inventory.noneEmptyItems;
            for (var i = 0; i < inputs.size(); i++) {
                stack.pushPose();
                {
                    var itemRotation = MathUtils.flipCircle(i * 360f / inputs.size());
                    var circleOffset = 0.0;
                    if (progress > 0) {
                        circleOffset = MathUtils.modifier(progress, processTime, 1) * 360 * 3 + oldCircleOffset;
                    } else {
                        circleOffset = playerAngle;
                        oldCircleOffset = circleOffset;
                    }

                    var rotationDiff = MathUtils.singleRotation(axisRotation + itemRotation - circleOffset);
                    if (rotationDiff > 180) rotationDiff = 360 - rotationDiff;
                    var newHeight = (rotationDiff / 180) * MAX_ITEM_HEIGHT;

                    var playerOffset = Math.max(1 - altarPos.distanceTo(playerPos) / 8, 0);
                    newHeight *= playerOffset;

                    stack.mulPose(Vector3f.YN.rotationDegrees(MathUtils.singleRotation(itemRotation + axisRotation)));
                    stack.translate(0, newHeight, -ITEM_OFFSET);

                    var item = inputs.get(i);
                    if (!item.isEmpty) {
                        mc.itemRenderer
                            .renderStatic(
                                item,
                                TransformType.FIXED,
                                lightAbove,
                                overlay,
                                stack,
                                buffer,
                                (int) entity.blockPos.asLong()
                            );
                    }
                }
                stack.popPose();
            }

            if (processTime > 0 && progress >= processTime) {
                resetTimer = MAX_RESET;
            }
        }
        stack.popPose();
    }
}

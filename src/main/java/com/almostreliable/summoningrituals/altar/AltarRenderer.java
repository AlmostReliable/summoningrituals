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

public class AltarRenderer implements BlockEntityRenderer<AltarEntity> {

    private static final int MAX_DISTANCE = 32;
    private static final int HEIGHT_SHIFT = 2;
    private static final float HALF = .5f;

    private final Minecraft mc;

    public AltarRenderer(Context ignoredContext) {
        mc = Minecraft.getInstance();
    }

    @Override
    public void render(
        AltarEntity entity, float partial, PoseStack stack, MultiBufferSource buffer, int light, int overlay
    ) {
        if (mc.player == null || entity.getLevel() == null ||
            entity.getBlockPos().distSqr(mc.player.blockPosition()) > Math.pow(MAX_DISTANCE, 2)) {
            return;
        }

        var lightAbove = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().above());

        stack.pushPose();
        {
            stack.translate(HALF, HALF, HALF);
            stack.scale(HALF, HALF, HALF);

            if (!entity.inventory.getCatalyst().isEmpty()) {
                mc.getItemRenderer()
                    .renderStatic(
                        entity.inventory.getCatalyst(),
                        TransformType.FIXED,
                        lightAbove,
                        overlay,
                        stack,
                        buffer,
                        1
                    );
            }

            var altarPos = MathUtils.shiftToCenter(MathUtils.vectorFromBlockPos(entity.getBlockPos()));
            var playerPos = mc.player.position();
            var theta = Math.toDegrees(Math.atan2(altarPos.x - playerPos.x, playerPos.z - altarPos.z)) + 180;

            var axisRotation = entity.getLevel().getGameTime() % 360;

            var inputs = entity.inventory.getItems();
            for (var i = 0; i < inputs.size(); i++) {
                stack.pushPose();
                {
                    var rotation = 360 - (i * 360f / inputs.size());

                    var diff = Math.abs(MathUtils.ensureDegree(axisRotation + rotation) - theta);
                    if (diff > 180) diff = 360 - diff;
                    var newHeight = (diff / 180) * HEIGHT_SHIFT;

                    stack.mulPose(Vector3f.YN.rotationDegrees(MathUtils.ensureDegree(rotation + axisRotation)));
                    stack.translate(0, newHeight, -1.5f);
                    var item = inputs.get(i);
                    if (!item.isEmpty()) {
                        mc.getItemRenderer()
                            .renderStatic(
                                item,
                                TransformType.FIXED,
                                lightAbove,
                                overlay,
                                stack,
                                buffer,
                                (int) entity.getBlockPos().asLong()
                            );
                    }
                }
                stack.popPose();
            }
        }
        stack.popPose();
    }
}

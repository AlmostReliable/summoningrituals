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

public class AltarRenderer implements BlockEntityRenderer<AltarEntity> {

    private static final int MAX_DISTANCE = 32;
    private static final int HEIGHT_SHIFT = 2;
    private static final float HALF = .5f;
    private static final float ITEM_OFFSET = 1.5f;

    private final Minecraft mc;
    private final ItemRenderer itemRenderer;

    public AltarRenderer(Context ignoredContext) {
        mc = Minecraft.getInstance();
        itemRenderer = mc.getItemRenderer();
    }

    @Override
    public void render(
        AltarEntity entity, float partial, PoseStack stack, MultiBufferSource buffer, int light, int overlay
    ) {
        if (mc.player == null || entity.getLevel() == null ||
            entity.getBlockPos().distSqr(mc.player.blockPosition()) > Math.pow(MAX_DISTANCE, 2)) {
            return;
        }

        stack.pushPose();
        {
            stack.translate(HALF, 0.8f, HALF);
            stack.scale(HALF, HALF, HALF);

            var lightAbove = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().above());

            stack.pushPose();
            {
                stack.translate(0, Math.sin(entity.getLevel().getGameTime() / 5f) / 10 + 1, 0);
                stack.scale(0.8f, 0.8f, 0.8f);
                stack.mulPose(Vector3f.YN.rotationDegrees(mc.player.getYHeadRot()));
                if (!entity.inventory.getCatalyst().isEmpty()) {
                    itemRenderer
                        .renderStatic(
                            entity.inventory.getCatalyst(),
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

            var altarPos = MathUtils.shiftToCenter(MathUtils.vectorFromBlockPos(entity.getBlockPos()));
            var playerPos = mc.player.position();
            var playerAngle = Math.toDegrees(Math.atan2(altarPos.x - playerPos.x, playerPos.z - altarPos.z)) + 180;
            var axisRotation = MathUtils.ensureDegree(entity.getLevel().getGameTime());

            var inputs = entity.inventory.getItems();
            for (var i = 0; i < inputs.size(); i++) {
                stack.pushPose();
                {
                    var itemRotation = 360 - (i * 360f / inputs.size());

                    var rotationDiff = Math.abs(MathUtils.ensureDegree(axisRotation + itemRotation) - playerAngle);
                    if (rotationDiff > 180) rotationDiff = 360 - rotationDiff;
                    var newHeight = (rotationDiff / 180) * HEIGHT_SHIFT;

                    var playerOffset = Math.max(1 - altarPos.distanceTo(playerPos) / 8, 0);
                    newHeight *= playerOffset;

                    stack.mulPose(Vector3f.YN.rotationDegrees(MathUtils.ensureDegree(itemRotation + axisRotation)));
                    stack.translate(0, newHeight, -ITEM_OFFSET);

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

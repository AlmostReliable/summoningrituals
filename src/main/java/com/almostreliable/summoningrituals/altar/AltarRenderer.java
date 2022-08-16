package com.almostreliable.summoningrituals.altar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;

public class AltarRenderer implements BlockEntityRenderer<AltarEntity> {

    private static final int MAX_DISTANCE = 30;
    private static final float HALF = .5f;

    private final Minecraft mc;

    public AltarRenderer(Context context) {
        mc = Minecraft.getInstance();
    }

    @Override
    public void render(
        AltarEntity entity, float partial, PoseStack stack, MultiBufferSource buffer, int light, int overlay
    ) {
        if (mc.player == null || entity.getBlockPos().distSqr(mc.player.blockPosition()) > Math.pow(MAX_DISTANCE, 2) ||
            entity.inventory.getInputs().isEmpty()) {
            return;
        }

        stack.pushPose();

        stack.translate(HALF, 2, HALF);

        var inputs = entity.inventory.getInputs();
        for (var i = 0; i < inputs.size(); i++) {
            var item = inputs.get(i);
            if (!item.isEmpty()) {
                mc.getItemRenderer()
                    .renderStatic(
                        item,
                        TransformType.GROUND,
                        light,
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

package com.almostreliable.summoningrituals.altar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
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
        if (mc.player == null || entity.getLevel() == null ||
            entity.getBlockPos().distSqr(mc.player.blockPosition()) > Math.pow(MAX_DISTANCE, 2)) {
            return;
        }

        stack.pushPose();

        stack.translate(HALF, 1.5, HALF);
        stack.scale(.6f, .6f, .6f);

        var lightAbove = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().above());
        var axis = new Vector3f(0, 1, 0);
        stack.mulPose(axis.rotation(entity.getLevel().getGameTime() / 50f));

        if (!entity.inventory.getCatalyst().isEmpty()) {
            mc.getItemRenderer()
                .renderStatic(entity.inventory.getCatalyst(),
                    TransformType.FIXED,
                    lightAbove,
                    overlay,
                    stack,
                    buffer,
                    1
                );
        }

        var inputs = entity.inventory.getInputs();
        for (var i = 0; i < inputs.size(); i++) {
            stack.pushPose();
            stack.mulPose(axis.rotation(i * 360f / inputs.size()));
            stack.translate(0, 0, 1.5f);
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
            stack.popPose();
        }

        stack.popPose();
    }
}

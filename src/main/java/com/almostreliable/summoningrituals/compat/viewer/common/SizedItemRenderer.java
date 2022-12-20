package com.almostreliable.summoningrituals.compat.viewer.common;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.List;

public class SizedItemRenderer {

    protected final int size;
    final Minecraft mc;
    private final ItemRenderer itemRenderer;

    SizedItemRenderer(int size) {
        mc = Minecraft.instance;
        this.itemRenderer = mc.itemRenderer;
        this.size = size;
    }

    public void render(PoseStack stack, @Nullable ItemStack item) {
        if (item == null) return;
        PoseStack modelViewStack = RenderSystem.modelViewStack;
        modelViewStack.pushPose();
        {
            modelViewStack.mulPoseMatrix(stack.last().pose());
            var scale = size / 16f;
            modelViewStack.scale(scale, scale, scale);
            RenderSystem.enableDepthTest();
            itemRenderer.renderAndDecorateFakeItem(item, 0, 0);
            itemRenderer.renderGuiItemDecorations(mc.font, item, 0, 0);
            RenderSystem.disableBlend();
        }
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public List<Component> getTooltip(ItemStack stack, TooltipFlag tooltipFlag) {
        return List.of();
    }
}

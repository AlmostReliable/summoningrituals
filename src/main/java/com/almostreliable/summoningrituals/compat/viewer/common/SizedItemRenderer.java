package com.almostreliable.summoningrituals.compat.viewer.common;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.List;

public class SizedItemRenderer {

    protected final int size;
    final Minecraft mc;

    SizedItemRenderer(int size) {
        mc = Minecraft.getInstance();
        this.size = size;
    }

    public void render(GuiGraphics guiGraphics, @Nullable ItemStack item) {
        if (item == null) return;
        PoseStack stack = guiGraphics.pose();
        stack.pushPose();
        {
            var scale = size / 16f;
            stack.scale(scale, scale, scale);
            RenderSystem.enableDepthTest();
            guiGraphics.renderFakeItem(item, 0, 0);
            guiGraphics.renderItemDecorations(mc.font, item, 0, 0);
            RenderSystem.disableBlend();
        }
        stack.popPose();
    }

    public List<Component> getTooltip(ItemStack stack, TooltipFlag tooltipFlag) {
        return List.of();
    }
}

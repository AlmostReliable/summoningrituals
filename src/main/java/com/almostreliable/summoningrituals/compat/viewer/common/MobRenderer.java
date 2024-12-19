package com.almostreliable.summoningrituals.compat.viewer.common;

import com.almostreliable.summoningrituals.util.GameUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MobRenderer {

    private static final float CREEPER_HEIGHT = 1.7f;
    private static final float CREEPER_SCALE = 0.5f;

    protected final int size;
    private final Minecraft mc;

    protected MobRenderer(int size) {
        this.size = size;
        mc = Minecraft.getInstance();
    }

    public void render(GuiGraphics guiGraphics, @Nullable EntityIngredient mob) {
        if (mc.level == null || mc.player == null || mob == null) return;
        PoseStack stack = guiGraphics.pose();
        if (mob.getEntity() != null && mob.getEntity() instanceof LivingEntity entity) {
            stack.pushPose();
            entity.tickCount = mc.player.tickCount;
            stack.translate(0.5f * size, 0.9f * size, 0);
            var entityHeight = entity.getBbHeight();
            var entityScale = Math.min(CREEPER_HEIGHT / entityHeight, 1f);
            var scaleFactor = CREEPER_SCALE * size * entityScale;
            renderEntity(guiGraphics, entity, scaleFactor);
            stack.popPose();
        }
        if (mob.getCount() > 1) {
            var count = String.valueOf(mob.getCount());
            GameUtils.renderCount(guiGraphics, count, size, size);
        }
    }

    private void renderEntity(GuiGraphics guiGraphics, LivingEntity entity, float scaleFactor) {
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.mulPoseMatrix(guiGraphics.pose().last().pose());
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, 0, 0, (int) scaleFactor, 75, -20, entity);
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public List<Component> getTooltip(EntityIngredient mob, TooltipFlag tooltipFlag) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(mob.getDisplayName());
        if (tooltipFlag.isAdvanced()) {
            tooltip.add(mob.getRegistryName().withStyle(ChatFormatting.DARK_GRAY));
        }
        return tooltip;
    }
}

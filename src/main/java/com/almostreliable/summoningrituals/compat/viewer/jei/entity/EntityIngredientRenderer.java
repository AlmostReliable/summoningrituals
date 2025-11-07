package com.almostreliable.summoningrituals.compat.viewer.jei.entity;

import com.almostreliable.summoningrituals.core.Config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.TooltipFlag;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mezz.jei.api.ingredients.IIngredientRenderer;

import java.util.ArrayList;
import java.util.List;

public class EntityIngredientRenderer implements IIngredientRenderer<EntityIngredient> {

    private static final int TEXT_COLOR = 16_777_215;
    private final Minecraft mc = Minecraft.getInstance();

    @Override
    public void render(GuiGraphics guiGraphics, EntityIngredient entityIngredient) {
        if (mc.level == null || mc.player == null || !(entityIngredient.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        // animate entity
        entity.tickCount = mc.player.tickCount;

        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        {
            renderEntity(guiGraphics, entityIngredient, entity, poseStack);
        }
        poseStack.popPose();
        poseStack.pushPose();
        {
            renderCount(guiGraphics, entityIngredient, poseStack);
        }
        poseStack.popPose();
    }

    @SuppressWarnings("deprecation")
    private void renderEntity(
        GuiGraphics guiGraphics, EntityIngredient entityIngredient, LivingEntity entity, PoseStack poseStack
    ) {
        var entityId = entityIngredient.getResourceLocation().toString();

        // move entity to the slot center with configurable y offset
        var yOffset = Config.CLIENT.entityOffsets.getOrDefault(entityId, 0f);
        poseStack.translate(8, 15 + yOffset, 100);

        // rotate entity to be slightly tilted to down left
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(20));
        poseStack.mulPose(Axis.XP.rotationDegrees(5));

        // scale entity to fit in the slot depending on its bounding box and config override value
        var defaultScale = (float) (9 * Math.pow(entity.getBoundingBox().getSize(), -1));
        var scale = Config.CLIENT.entitySizes.getOrDefault(entityId, defaultScale);
        poseStack.summoning$scale(scale);

        // set up renderer
        Lighting.setupForEntityInInventory();
        var entityRenderer = mc.getEntityRenderDispatcher();
        entityRenderer.setRenderShadow(false);
        RenderSystem.enableBlend();

        // render entity
        RenderSystem.runAsFancy(() -> entityRenderer.render(
            entity, 0, 0, 0, 0, 1, poseStack, guiGraphics.bufferSource(),
            LightTexture.FULL_BRIGHT
        ));
        guiGraphics.flush();

        // reset renderer
        entityRenderer.setRenderShadow(true);
        Lighting.setupFor3DItems();
    }

    private void renderCount(GuiGraphics guiGraphics, EntityIngredient entityIngredient, PoseStack poseStack) {
        var count = entityIngredient.getEntityInfo().count();
        if (count <= 1) return;
        poseStack.translate(10, 9, 200);
        guiGraphics.drawString(mc.font, String.valueOf(count), 0, 0, TEXT_COLOR, true);
    }

    @Override
    public List<Component> getTooltip(EntityIngredient entity, TooltipFlag tooltipFlag) {
        var tooltip = new ArrayList<Component>();
        tooltip.add(entity.getDisplayName());

        var entityTooltip = entity.getEntityInfo().tooltip();
        if (!entityTooltip.isEmpty()) {
            tooltip.addAll(entityTooltip);
        }

        if (tooltipFlag.isAdvanced()) {
            var id = Component.literal(entity.getResourceLocation().toString());
            tooltip.add(id.withStyle(ChatFormatting.DARK_GRAY));
        }
        return tooltip;
    }
}

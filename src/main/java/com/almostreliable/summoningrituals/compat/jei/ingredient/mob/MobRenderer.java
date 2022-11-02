package com.almostreliable.summoningrituals.compat.jei.ingredient.mob;

import com.almostreliable.summoningrituals.util.GameUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MobRenderer implements IIngredientRenderer<MobIngredient> {

    private static final float CREEPER_HEIGHT = 1.7f;
    private static final float CREEPER_SCALE = 0.5f;

    private final Minecraft mc;
    private final EntityRenderDispatcher entityRenderer;
    private final int size;

    public MobRenderer(int size) {
        mc = Minecraft.getInstance();
        entityRenderer = mc.getEntityRenderDispatcher();
        this.size = size;
    }

    @Override
    public void render(PoseStack stack, @Nullable MobIngredient mob) {
        if (mc.level == null || mc.player == null || mob == null) return;
        if (mob.getEntity() != null && mob.getEntity() instanceof LivingEntity entity) {
            stack.pushPose();
            entity.tickCount = mc.player.tickCount;
            stack.translate(0.5f * size, 0.9f * size, 0);
            var entityHeight = entity.getBbHeight();
            var entityScale = Math.min(CREEPER_HEIGHT / entityHeight, 1f);
            var scaleFactor = CREEPER_SCALE * size * entityScale;
            // renderOld(stack, entity, scaleFactor);
            renderEntity(stack, entity, scaleFactor);
            stack.popPose();
        }
        if (mob.getCount() > 1) {
            var count = String.valueOf(mob.getCount());
            GameUtils.renderCount(stack, count, size, size);
        }
    }

    // private void renderOld(PoseStack stack, LivingEntity entity, float scaleFactor) {
    //     stack.scale(scaleFactor, scaleFactor, scaleFactor);
    //     stack.mulPose(Vector3f.ZN.rotationDegrees(180));
    //     stack.mulPose(Vector3f.XP.rotationDegrees(30));
    //     stack.mulPose(Vector3f.YP.rotationDegrees(45));
    //     var bufferSource = mc.renderBuffers().bufferSource();
    //     entityRenderer.render(entity, 0, 0, 0, mc.getFrameTime(), 1, stack, bufferSource, LightTexture.FULL_BRIGHT);
    //     bufferSource.endBatch();
    // }

    private void renderEntity(PoseStack stack, LivingEntity entity, float scaleFactor) {
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.mulPoseMatrix(stack.last().pose());
        InventoryScreen.renderEntityInInventory(0, 0, (int) scaleFactor, 75, -20, entity);
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public List<Component> getTooltip(MobIngredient mob, TooltipFlag tooltipFlag) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(mob.getDisplayName());
        if (tooltipFlag.isAdvanced()) {
            tooltip.add(mob.getRegistryName().withStyle(ChatFormatting.DARK_GRAY));
        }
        return tooltip;
    }

    @Override
    public int getWidth() {
        return size;
    }

    @Override
    public int getHeight() {
        return size;
    }
}

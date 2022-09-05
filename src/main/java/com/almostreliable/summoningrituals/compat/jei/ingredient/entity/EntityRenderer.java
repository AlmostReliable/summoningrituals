package com.almostreliable.summoningrituals.compat.jei.ingredient.entity;

import com.almostreliable.summoningrituals.util.GameUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class EntityRenderer implements IIngredientRenderer<EntityIngredient> {

    private static final float CREEPER_HEIGHT = 1.7f;
    private static final float CREEPER_SCALE = 0.5f;

    private final Minecraft mc;
    private final EntityRenderDispatcher entityRenderer;
    private final Player player;
    private final int size;

    public EntityRenderer(int size) {
        mc = Minecraft.getInstance();
        entityRenderer = mc.getEntityRenderDispatcher();
        this.player = mc.player;
        this.size = size;
    }

    @Override
    public void render(PoseStack stack, EntityIngredient ingredient) {
        if (mc.level == null || ingredient.getEntity() == null) return;
        stack.pushPose();
        {
            var entity = ingredient.getEntity();
            entity.tickCount = player.tickCount;
            stack.translate(0.5f * size, 0.85f * size, 0);
            var entityHeight = entity.getBbHeight();
            var entityScale = Math.min(CREEPER_HEIGHT / entityHeight, 1f);
            var scaleFactor = CREEPER_SCALE * size * entityScale;
            stack.scale(scaleFactor, scaleFactor, scaleFactor);
            stack.mulPose(Vector3f.ZN.rotationDegrees(180));
            stack.mulPose(Vector3f.XP.rotationDegrees(30));
            stack.mulPose(Vector3f.YP.rotationDegrees(45));
            var bufferSource = mc.renderBuffers().bufferSource();
            entityRenderer.render(entity, 0, 0, 0, mc.getFrameTime(), 1, stack, bufferSource, LightTexture.FULL_BRIGHT);
            bufferSource.endBatch();
        }
        stack.popPose();
        if (ingredient.getCount() > 1) {
            var count = String.valueOf(ingredient.getCount());
            GameUtils.renderCount(stack, count, size, size);
        }
    }

    @Override
    public List<Component> getTooltip(EntityIngredient ingredient, TooltipFlag tooltipFlag) {
        return List.of(ingredient.getDisplayName());
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

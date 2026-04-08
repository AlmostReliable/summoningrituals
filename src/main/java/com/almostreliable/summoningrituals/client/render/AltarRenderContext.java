package com.almostreliable.summoningrituals.client.render;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;

import java.util.List;

public record AltarRenderContext(
    Level getLevel,
    Player getPlayer,
    AltarBlockEntity getAltar,
    float getPlayerToAltarDistance,
    float getPlayerToAltarAngle,
    float getRecipeProgress,
    float getRecipeTime,
    float getRecipeProgressRatio,
    PoseStack getPoseStack,
    MultiBufferSource getRenderBuffer,
    int getLightAbove,
    int getPackedOverlay,
    float getPartialTick
) {

    public void pushPose() {
        getPoseStack().pushPose();
    }

    public void popPose() {
        getPoseStack().popPose();
    }

    public void translate(float x, float y, float z) {
        getPoseStack().translate(x, y, z);
    }

    public void scale(float x, float y, float z) {
        getPoseStack().scale(x, y, z);
    }

    public void scale(float scale) {
        getPoseStack().scale(scale, scale, scale);
    }

    public void mulPose(Quaternionf quaternion) {
        getPoseStack().mulPose(quaternion);
    }

    public boolean shouldReset() {
        return getRecipeTime > 0 && getRecipeProgress >= getRecipeTime;
    }

    public ItemStack getInitiator() {
        return getAltar().getInventory().getInitiator();
    }

    public List<ItemStack> getInputs() {
        return getAltar().getInventory().getDisplayItems();
    }

    public void renderItem(ItemRenderer itemRenderer, ItemStack item) {
        itemRenderer.renderStatic(
            item,
            ItemDisplayContext.FIXED,
            getLightAbove,
            getPackedOverlay,
            getPoseStack,
            getRenderBuffer,
            getLevel,
            0
        );
    }
}

package com.almostreliable.summoningrituals.client.render;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
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
    int getRecipeProgress,
    int getRecipeTime,
    PoseStack getPoseStack,
    SubmitNodeCollector getSubmitNodeCollector,
    int getLightAbove,
    float getPartialTick,
    ItemStackRenderState getInitiatorRenderState,
    List<ItemStackRenderState> getInputRenderStates
) {

    public float getRecipeProgressRatio() {
        if (getRecipeTime <= 0) return 0f;

        var craftStartTick = getAltar.craftStartTick;
        if (craftStartTick < 0) {
            return Mth.clamp((getRecipeProgress + getPartialTick) / getRecipeTime, 0f, 1f);
        }

        var predictedProgress = (getGameTime() - craftStartTick) + getPartialTick;
        return Mth.clamp(predictedProgress / getRecipeTime, 0f, 1f);
    }

    public long getGameTime() {
        return getLevel.getGameTime();
    }

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

    public void renderItem(ItemModelResolver itemModelResolver, ItemStack item) {
        var itemRenderState = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(itemRenderState, item, ItemDisplayContext.FIXED, getLevel, null, 0);
        renderItem(itemRenderState);
    }

    public void renderItem(ItemStackRenderState itemRenderState) {
        itemRenderState.submit(getPoseStack, getSubmitNodeCollector, getLightAbove, OverlayTexture.NO_OVERLAY, 0);
    }
}

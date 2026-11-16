package com.almostreliable.summoningrituals.client.render;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;

import org.jspecify.annotations.Nullable;

import java.util.List;

public record AltarRenderContext(
    Level getLevel,
    Player getPlayer,
    BlockPos getAltarPos,
    long getGameTime,
    float getPartialTick,
    float getPlayerToAltarDistance,
    float getPlayerToAltarAngle,
    int getRecipeProgress,
    int getRecipeTime,
    float getRecipeProgressRatio,
    float getOrbitScale,
    float getOrbitWaveAnchor,
    int getLightAbove,
    ItemStack getInitiator,
    List<ItemStack> getInputs,
    ItemStackRenderState getInitiatorRenderState,
    List<ItemStackRenderState> getInputRenderStates,
    PoseStack getPoseStack,
    SubmitNodeCollector getSubmitNodeCollector
) {

    @Nullable
    public AltarBlockEntity getAltar() {
        var blockEntity = getLevel.getBlockEntity(getAltarPos);
        return blockEntity instanceof AltarBlockEntity altar ? altar : null;
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

    public void renderItem(ItemModelResolver itemModelResolver, ItemStack item) {
        var itemRenderState = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(itemRenderState, item, ItemDisplayContext.FIXED, getLevel, null, 0);
        renderItem(itemRenderState);
    }

    public void renderItem(ItemStackRenderState itemRenderState) {
        itemRenderState.submit(getPoseStack, getSubmitNodeCollector, getLightAbove, OverlayTexture.NO_OVERLAY, 0);
    }
}

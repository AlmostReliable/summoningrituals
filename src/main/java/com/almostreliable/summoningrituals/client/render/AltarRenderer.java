package com.almostreliable.summoningrituals.client.render;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.core.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess") // exposed for custom KubeJS renderers
public class AltarRenderer implements BlockEntityRenderer<AltarBlockEntity> {

    public static final Map<ResourceLocation, CustomRitualRenderer> CUSTOM_RENDERERS = new HashMap<>();
    public static final float HALF = .5f;
    public static final float HALF_CIRCLE = 180f;
    public static final float FULL_CIRCLE = 360f;
    public static final float ALTAR_RENDER_HEIGHT = 0.8f;
    private static final int MAX_ITEM_HEIGHT = 2;
    private static final int MAX_RESET = 60;
    private static final float MAX_PROGRESS_HEIGHT = 2.5f;
    private static final float ITEM_OFFSET = 1.5f;

    private final ItemRenderer itemRenderer;

    private float resetTimer;
    private float oldCircleOffset;

    public AltarRenderer(Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
        AltarBlockEntity altar, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay
    ) {
        var player = Minecraft.getInstance().player;
        var level = altar.getLevel();

        if (player == null || level == null ||
            !altar.getBlockPos().closerThan(player.blockPosition(), Config.CLIENT.renderDistance.getAsInt())) {
            return;
        }

        CustomRitualRenderer customRenderer = null;
        var recipeInfo = altar.getCurrentRecipeInfo();
        if (recipeInfo != null) {
            customRenderer = CUSTOM_RENDERERS.get(recipeInfo.recipeId());
        }
        var renderContext = createRenderContext(poseStack, buffer, altar, player, level, partialTick, packedOverlay);

        renderContext.pushPose();
        {
            if (customRenderer != null) {
                try {
                    customRenderer.render(this, recipeInfo.recipe(), renderContext);
                } catch (Exception e) {
                    CUSTOM_RENDERERS.remove(recipeInfo.recipeId());
                    SummoningRituals.LOGGER.error("failed to render custom ritual: {}", recipeInfo.recipeId(), e);
                }
            } else {
                renderContext.translate(HALF, ALTAR_RENDER_HEIGHT, HALF);
                renderContext.scale(HALF);
                renderInventoryContents(renderContext);
            }
        }
        renderContext.popPose();
    }

    private AltarRenderContext createRenderContext(
        PoseStack poseStack, MultiBufferSource buffer, AltarBlockEntity altar, Player player, Level level, float partialTick,
        int packedOverlay
    ) {
        var altarPos = altar.getBlockPos();
        var altarCenterPos = Vec3.atCenterOf(altarPos);

        var playerPos = player.position();
        var playerToAltarDistance = (float) altarCenterPos.distanceTo(playerPos);
        var playerToAltarRatio = Math.atan2(altarCenterPos.x - playerPos.x, playerPos.z - altarCenterPos.z);
        var playerToAltarAngle = (float) (Math.toDegrees(playerToAltarRatio) + HALF_CIRCLE);

        var recipeProgress = altar.getRecipeProgress();
        var recipeTime = altar.getRecipeTime();
        var recipeProgressRatio = ratio(recipeProgress, recipeTime, 0f);

        var lightAbove = LevelRenderer.getLightColor(level, altarPos.above());

        return new AltarRenderContext(
            level,
            player,
            altar,
            playerToAltarDistance,
            playerToAltarAngle,
            recipeProgress,
            recipeTime,
            recipeProgressRatio,
            poseStack,
            buffer,
            lightAbove,
            packedOverlay,
            partialTick
        );
    }

    private void renderInventoryContents(AltarRenderContext renderContext) {
        renderContext.translate(0, MAX_PROGRESS_HEIGHT * renderContext.getRecipeProgressRatio(), 0);

        renderInitiator(renderContext);
        renderItemOrbit(renderContext);

        if (renderContext.shouldReset()) {
            resetTimer = MAX_RESET;
        }
    }

    public void renderInitiator(AltarRenderContext renderContext) {
        var initiator = renderContext.getInitiator();
        if (initiator.isEmpty()) return;

        renderContext.pushPose();
        {
            renderContext.translate(0, invert(0.75f * renderContext.getRecipeProgressRatio()), 0);
            renderContext.scale(0.75f);
            renderContext.mulPose(Axis.YN.rotationDegrees(renderContext.getPlayerToAltarAngle()));
            renderContext.renderItem(itemRenderer, initiator);
        }
        renderContext.popPose();
    }

    public void renderItemOrbit(AltarRenderContext renderContext) {
        var inputs = renderContext.getInputs();
        if (inputs.isEmpty()) return;

        var axisRotation = clampRotation(renderContext.getLevel().getGameTime());
        var recipeProgress = renderContext.getRecipeProgress();
        var scale = invert(renderContext.getRecipeProgressRatio());
        if (recipeProgress == 0 && resetTimer > 0) {
            scale = invert(ratio(resetTimer, MAX_RESET, 0f));
            resetTimer = Math.max(0, resetTimer - renderContext.getPartialTick());
        }

        renderContext.scale(scale);

        for (var i = 0; i < inputs.size(); i++) {
            renderContext.pushPose();
            {
                var itemRotation = FULL_CIRCLE - ((i * FULL_CIRCLE) / inputs.size());

                float circleOffset;
                if (recipeProgress > 0) {
                    circleOffset = ratio(recipeProgress, renderContext.getRecipeTime(), 1f) * FULL_CIRCLE * 3f + oldCircleOffset;
                } else {
                    circleOffset = renderContext.getPlayerToAltarAngle();
                    oldCircleOffset = circleOffset;
                }

                var rotationDiff = clampRotation(axisRotation + itemRotation - circleOffset);
                if (rotationDiff > HALF_CIRCLE) rotationDiff = FULL_CIRCLE - rotationDiff;
                var newHeight = (rotationDiff / HALF_CIRCLE) * MAX_ITEM_HEIGHT;

                var playerOffset = Math.max(1f - renderContext.getPlayerToAltarDistance() / 8f, 0f);
                newHeight *= playerOffset;

                renderContext.mulPose(Axis.YN.rotationDegrees(clampRotation(itemRotation + axisRotation)));
                renderContext.translate(0, newHeight, -ITEM_OFFSET);

                renderContext.renderItem(itemRenderer, inputs.get(i));
            }
            renderContext.popPose();
        }
    }

    public static float clampRotation(float degree) {
        return Math.abs(degree) % FULL_CIRCLE;
    }

    public static float invert(float value) {
        return 1 - value;
    }

    public static float ratio(float current, float max, float fallback) {
        return max == 0f ? fallback : current / max;
    }

    // exposed for KubeJS
    @SuppressWarnings("unused")
    public ItemRenderer getItemRenderer() {
        return itemRenderer;
    }
}

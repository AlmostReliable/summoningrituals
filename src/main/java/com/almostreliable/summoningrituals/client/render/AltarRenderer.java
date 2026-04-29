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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess") // exposed for custom KubeJS renderers
public record AltarRenderer(ItemRenderer getItemRenderer) implements BlockEntityRenderer<AltarBlockEntity> {

    public static final Map<ResourceLocation, CustomRitualRenderer> CUSTOM_RENDERERS = new HashMap<>();
    public static final float HALF = .5f;
    public static final float HALF_CIRCLE = 180f;
    public static final float FULL_CIRCLE = 360f;
    public static final float ALTAR_RENDER_HEIGHT = 0.8f;
    private static final int MAX_ITEM_HEIGHT = 2;
    private static final int RESET_TICKS = 20;
    private static final float ORBIT_DEGREES_PER_SECOND = 20f;
    private static final float MAX_PROGRESS_HEIGHT = 2.5f;
    private static final float ITEM_OFFSET = 1.5f;

    public AltarRenderer(Context itemRenderer) {
        this(itemRenderer.getItemRenderer());
    }

    @Override
    public void render(
        AltarBlockEntity altar, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay
    ) {
        var player = Minecraft.getInstance().player;
        var level = altar.getLevel();

        if (player == null || level == null ||
            !altar.getBlockPos().closerThan(player.blockPosition(), Config.CLIENT.inventoryRenderDistance.getAsInt())) {
            return;
        }

        CustomRitualRenderer customRenderer = null;
        var recipeInfo = altar.getCurrentRecipeInfo();
        if (recipeInfo != null) {
            customRenderer = CUSTOM_RENDERERS.get(recipeInfo.getRecipeId());
        }
        var renderContext = createRenderContext(poseStack, buffer, altar, player, level, partialTick, packedOverlay);

        renderContext.pushPose();
        {
            if (customRenderer != null) {
                try {
                    customRenderer.render(this, recipeInfo.getRecipe(), renderContext);
                } catch (Exception e) {
                    CUSTOM_RENDERERS.remove(recipeInfo.getRecipeId());
                    SummoningRituals.LOGGER.error("failed to render custom ritual: {}", recipeInfo.getRecipeId(), e);
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

        var playerPos = new Vec3(
            Mth.lerp(partialTick, player.xOld, player.getX()),
            Mth.lerp(partialTick, player.yOld, player.getY()),
            Mth.lerp(partialTick, player.zOld, player.getZ())
        );
        var playerToAltarDistance = (float) altarCenterPos.distanceTo(playerPos);
        var playerToAltarRatio = Math.atan2(altarCenterPos.x - playerPos.x, playerPos.z - altarCenterPos.z);
        var playerToAltarAngle = (float) (Math.toDegrees(playerToAltarRatio) + HALF_CIRCLE);

        var recipeProgress = altar.getRecipeProgress();
        var recipeTime = altar.getRecipeTime();

        var lightAbove = LevelRenderer.getLightColor(level, altarPos.above());

        return new AltarRenderContext(
            level,
            player,
            altar,
            playerToAltarDistance,
            playerToAltarAngle,
            recipeProgress,
            recipeTime,
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
            renderContext.getAltar().resetStartTick = renderContext.getGameTime();
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
            renderContext.renderItem(getItemRenderer, initiator);
        }
        renderContext.popPose();
    }

    public void renderItemOrbit(AltarRenderContext renderContext) {
        var inputs = renderContext.getInputs();
        if (inputs.isEmpty()) return;

        var gameTime = renderContext.getGameTime();
        var partialTick = renderContext.getPartialTick();
        var altar = renderContext.getAltar();

        var orbitRotation = calculateOrbitRotation();
        var recipeProgress = renderContext.getRecipeProgress();
        var recipeProgressRatio = renderContext.getRecipeProgressRatio();
        var scale = invert(recipeProgressRatio);

        var resetStartTick = altar.resetStartTick;
        if (recipeProgress == 0 && resetStartTick >= 0) {
            var elapsed = gameTime - resetStartTick + partialTick;
            scale = Mth.clamp(elapsed / RESET_TICKS, 0f, 1f);
            if (scale >= 1f) {
                altar.resetStartTick = -1;
            }
        }

        renderContext.scale(scale);

        float waveAnchor;
        if (recipeProgress > 0) {
            waveAnchor = recipeProgressRatio * FULL_CIRCLE * 3f + altar.lastWaveAnchor;
        } else {
            waveAnchor = renderContext.getPlayerToAltarAngle();
            altar.lastWaveAnchor = waveAnchor;
        }

        for (var i = 0; i < inputs.size(); i++) {
            renderContext.pushPose();
            {
                var itemRotation = FULL_CIRCLE - ((i * FULL_CIRCLE) / inputs.size());

                var rotationDiff = clampRotation(orbitRotation + itemRotation - waveAnchor);
                if (rotationDiff > HALF_CIRCLE) rotationDiff = FULL_CIRCLE - rotationDiff;
                var waveProgress = rotationDiff / HALF_CIRCLE;
                var newHeight = HALF * invert(Mth.cos(Mth.PI * waveProgress)) * MAX_ITEM_HEIGHT;

                var playerOffset = Math.max(1f - renderContext.getPlayerToAltarDistance() / 8f, 0f);
                newHeight *= playerOffset;

                renderContext.mulPose(Axis.YN.rotationDegrees(clampRotation(itemRotation + orbitRotation)));
                renderContext.translate(0, newHeight, -ITEM_OFFSET);

                renderContext.renderItem(getItemRenderer, inputs.get(i));
            }
            renderContext.popPose();
        }
    }

    public float calculateOrbitRotation() {
        var minecraft = Minecraft.getInstance();
        var renderTicks = minecraft.levelRenderer.getTicks()
            + minecraft.getTimer().getGameTimeDeltaPartialTick(false);
        var renderSeconds = renderTicks / 20f;
        return clampRotation(ORBIT_DEGREES_PER_SECOND * renderSeconds);
    }

    public static float clampRotation(float degree) {
        return ((degree % FULL_CIRCLE) + FULL_CIRCLE) % FULL_CIRCLE;
    }

    public static float invert(float value) {
        return 1 - value;
    }
}

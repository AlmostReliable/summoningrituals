package com.almostreliable.summoningrituals.client;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.core.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class AltarRenderer implements BlockEntityRenderer<AltarBlockEntity> {

    private static final float HALF = .5f;
    private static final float HALF_CIRCLE = 180f;
    private static final float FULL_CIRCLE = 360f;
    private static final float ALTAR_RENDER_HEIGHT = 0.8f;
    private static final int MAX_ITEM_HEIGHT = 2;
    private static final int MAX_RESET = 60;
    private static final float MAX_PROGRESS_HEIGHT = 2.5f;
    private static final float ITEM_OFFSET = 1.5f;

    private final ItemRenderer itemRenderer;
    private final int altarRenderDistance;

    private float resetTimer;
    private float oldCircleOffset;

    public AltarRenderer(Context context) {
        itemRenderer = context.getItemRenderer();
        altarRenderDistance = Config.CLIENT.altarRenderDistance.get();
    }

    @Override
    public void render(
        AltarBlockEntity altar, float partialTick, PoseStack stack, MultiBufferSource buffer, int packedLight,
        int packedOverlay
    ) {
        Player player = Minecraft.getInstance().player;
        Level level = altar.getLevel();

        if (player == null || level == null || !altar.getBlockPos().closerThan(player.blockPosition(), altarRenderDistance)) {
            return;
        }

        stack.pushPose();
        {
            stack.translate(HALF, ALTAR_RENDER_HEIGHT, HALF);
            stack.summoning$scale(HALF);
            renderInventoryContents(stack, buffer, altar, player, level, partialTick, packedOverlay);
        }
        stack.popPose();
    }

    private void renderInventoryContents(
        PoseStack stack, MultiBufferSource buffer, AltarBlockEntity altar, Player player, Level level,
        float partialTick, int packedOverlay
    ) {
        BlockPos altarPos = altar.getBlockPos();
        Vec3 altarCenterPos = Vec3.atCenterOf(altarPos);

        Vec3 playerPos = player.position();
        float playerToAltarDistance = (float) altarCenterPos.distanceTo(playerPos);
        double playerToAltarRatio = Math.atan2(altarCenterPos.x - playerPos.x, playerPos.z - altarCenterPos.z);
        float playerToAltarAngle = (float) (Math.toDegrees(playerToAltarRatio) + HALF_CIRCLE);

        float recipeProgress = altar.getRecipeProgress();
        float recipeTime = altar.getRecipeTime();
        float recipeProgressRatio = ratio(recipeProgress, recipeTime, 0f);

        int lightAbove = LevelRenderer.getLightColor(level, altarPos.above());

        var renderContext = new RenderContext(
            altar,
            level,
            stack,
            buffer,
            lightAbove,
            packedOverlay,
            partialTick,
            playerToAltarAngle,
            recipeProgressRatio
        );

        stack.translate(0, MAX_PROGRESS_HEIGHT * recipeProgressRatio, 0);

        renderCatalyst(renderContext);
        renderItemOrbit(renderContext, recipeProgress, recipeTime, playerToAltarDistance);

        if (recipeTime > 0 && recipeProgress >= recipeTime) {
            resetTimer = MAX_RESET;
        }
    }

    private void renderCatalyst(RenderContext renderContext) {
        ItemStack catalyst = renderContext.altar.getInventory().getCatalyst();
        if (catalyst.isEmpty()) return;

        PoseStack stack = renderContext.stack;
        stack.pushPose();
        {
            stack.translate(0, invert(0.75f * renderContext.recipeProgressRatio), 0);
            stack.summoning$scale(0.75f);
            stack.mulPose(Axis.YN.rotationDegrees(renderContext.playerToAltarAngle));
            renderContext.renderStatic(itemRenderer, catalyst);
        }
        stack.popPose();
    }

    private void renderItemOrbit(RenderContext renderContext, float recipeProgress, float recipeTime, float playerToAltarDistance) {
        var inputs = renderContext.altar.getInventory().getNoneEmptyItems();
        if (inputs.isEmpty()) return;

        float axisRotation = clampRotation(renderContext.level.getGameTime());
        float scale = invert(renderContext.recipeProgressRatio);
        if (recipeProgress == 0 && resetTimer > 0) {
            scale = invert(ratio(resetTimer, MAX_RESET, 0f));
            resetTimer = Math.max(0, resetTimer - renderContext.partialTick);
        }

        PoseStack stack = renderContext.stack;
        stack.summoning$scale(scale);

        for (int i = 0; i < inputs.size(); i++) {
            stack.pushPose();
            {
                float itemRotation = FULL_CIRCLE - ((i * FULL_CIRCLE) / inputs.size());

                float circleOffset;
                if (recipeProgress > 0) {
                    circleOffset = ratio(recipeProgress, recipeTime, 1f) * FULL_CIRCLE * 3f + oldCircleOffset;
                } else {
                    circleOffset = renderContext.playerToAltarAngle;
                    oldCircleOffset = circleOffset;
                }

                float rotationDiff = clampRotation(axisRotation + itemRotation - circleOffset);
                if (rotationDiff > HALF_CIRCLE) rotationDiff = FULL_CIRCLE - rotationDiff;
                float newHeight = (rotationDiff / HALF_CIRCLE) * MAX_ITEM_HEIGHT;

                float playerOffset = Math.max(1f - playerToAltarDistance / 8f, 0f);
                newHeight *= playerOffset;

                stack.mulPose(Axis.YN.rotationDegrees(clampRotation(itemRotation + axisRotation)));
                stack.translate(0, newHeight, -ITEM_OFFSET);

                renderContext.renderStatic(itemRenderer, inputs.get(i));
            }
            stack.popPose();
        }
    }

    /**
     * Clamps the given rotation degree to a value between 0 (inclusive) and 360 (exclusive)
     * by calculating the absolute value of the degree and performing a modulus operation.
     *
     * @param degree The rotation degree to clamp.
     * @return The clamped rotation degree, ensuring it remains within the range [0, 360).
     */
    private static float clampRotation(float degree) {
        return Math.abs(degree) % FULL_CIRCLE;
    }

    /**
     * Inverts the given value.
     *
     * @param value The value to invert.
     * @return The inverted value.
     */
    private static float invert(float value) {
        return 1 - value;
    }

    /**
     * Calculates the ratio of {@code current} to {@code max}.
     * If {@code max} is 0, returns the specified {@code fallback} value instead to avoid division by zero.
     *
     * @param current  The current value to be divided.
     * @param max      The maximum value that acts as the divisor.
     * @param fallback The fallback value to return if {@code max} is 0.
     * @return The ratio of {@code current} to {@code max}, or {@code fallback} if {@code max} is 0.
     */
    private static float ratio(float current, float max, float fallback) {
        return max == 0f ? fallback : current / max;
    }

    private record RenderContext(
        AltarBlockEntity altar, Level level, PoseStack stack, MultiBufferSource buffer, int lightAbove, int packedOverlay,
        float partialTick, float playerToAltarAngle, float recipeProgressRatio
    ) {

        private void renderStatic(ItemRenderer itemRenderer, ItemStack item) {
            itemRenderer.renderStatic(item, ItemDisplayContext.FIXED, lightAbove, packedOverlay, stack, buffer, level, 0);
        }
    }
}

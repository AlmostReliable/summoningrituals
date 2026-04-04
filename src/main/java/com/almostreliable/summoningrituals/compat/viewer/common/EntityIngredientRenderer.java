package com.almostreliable.summoningrituals.compat.viewer.common;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.client.util.MeasuringBufferSource;
import com.almostreliable.summoningrituals.client.util.MeasuringBufferSource.MeasuringResult;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.Items;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

import static com.almostreliable.summoningrituals.compat.viewer.common.RecipeViewerAltarLayout.SLOT_SIZE;

/**
 * A renderer to handle rendering entities with a similar size and proper offset to fit a slot
 * bounding box. It supports options to hide the rendering of the count, as well as optional
 * clipping to predefined slot bounds.
 * <p>
 * The renderer makes use of the {@link MeasuringBufferSource} to measure the entity vertices.
 * <p>
 * license: Unlicense
 * <p>
 * Check the README license section for more information.
 */
public final class EntityIngredientRenderer {

    private static final Map<ResourceLocation, MeasuringResult> MEASURING_RESULT_CACHE = new HashMap<>();
    private static final int TEXT_COLOR = 16_777_215;
    private static final int MEASURE_TICKS = 40;
    private static final int HALF_ROT = 180;

    private EntityIngredientRenderer() {}

    @SuppressWarnings("BooleanParameter")
    public static void render(GuiGraphics guiGraphics, EntityIngredient entityIngredient, boolean scissor, boolean count) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !(entityIngredient.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        var measuringResult = measureEntity(mc, entity, entityIngredient.getId());
        if (measuringResult == MeasuringResult.EMPTY) {
            guiGraphics.renderItem(Items.BARRIER.getDefaultInstance(), 0, 0);
            return;
        }

        entity.tickCount = mc.player.tickCount;

        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        {
            renderEntity(mc, guiGraphics, entity, measuringResult, scissor);
        }
        poseStack.popPose();

        var entityCount = entityIngredient.getEntityInfo().count();
        if (!count || entityCount <= 1) return;
        poseStack.pushPose();
        {
            renderCount(mc, guiGraphics, entityCount);
        }
        poseStack.popPose();
    }

    @SuppressWarnings("deprecation")
    private static MeasuringResult measureEntity(Minecraft mc, LivingEntity entity, ResourceLocation entityId) {
        var cached = MEASURING_RESULT_CACHE.get(entityId);
        if (cached != null) return cached;

        var entityRenderer = mc.getEntityRenderDispatcher();
        var measuringBuffer = new MeasuringBufferSource();
        var poseStack = new PoseStack();

        // measure for 40 render ticks because size can change with animation
        try {
            for (var i = 0; i < MEASURE_TICKS; i++) {
                var ticks = i;
                RenderSystem.runAsFancy(() -> entityRenderer.render(
                    entity, 0, 0, 0, 0, ticks, poseStack, measuringBuffer, LightTexture.FULL_BRIGHT
                ));
            }
        } catch (Exception ignored) {
            return MeasuringResult.EMPTY;
        }

        var measuringResult = measuringBuffer.getData();
        if (measuringResult == null) {
            SummoningRituals.LOGGER.error("failed to measure entity: {}", entityId);
            return MeasuringResult.EMPTY;
        }
        MEASURING_RESULT_CACHE.put(entityId, measuringResult);
        return measuringResult;
    }

    @SuppressWarnings("deprecation")
    private static void renderEntity(
        Minecraft mc, GuiGraphics guiGraphics, LivingEntity entity, MeasuringResult measuringResult, boolean scissor
    ) {
        var poseStack = guiGraphics.pose();
        if (scissor) {
            // gui graphics scissor doesn't take pose stack into account, bruh
            var absolutePos = poseStack.last().pose().transformPosition(0, 0, 0, new Vector3f());
            var absX = (int) absolutePos.x;
            var absY = (int) absolutePos.y;
            guiGraphics.enableScissor(absX, absY, absX + SLOT_SIZE, absY + SLOT_SIZE);
        }

        poseStack.translate(SLOT_SIZE / 2f, SLOT_SIZE, 100);
        poseStack.mulPose(Axis.ZP.rotationDegrees(HALF_ROT));

        Lighting.setupForEntityInInventory();
        var entityRenderer = mc.getEntityRenderDispatcher();
        entityRenderer.setRenderShadow(false);
        RenderSystem.enableBlend();

        // need to turn all mobs to the other side because the dragon and the bat have reversed models
        // this requires inverting the models later by a negative z-value;
        // tried so many other approaches, this is the only thing that works
        entity.absRotateTo(HALF_ROT, 0);
        entity.yBodyRotO = HALF_ROT;
        entity.yBodyRot = HALF_ROT;
        entity.yHeadRotO = HALF_ROT;
        entity.yHeadRot = HALF_ROT;
        if (entity instanceof WitherBoss witherBoss) {
            // this is usually handled in the wither AI for whatever reason, Mojank
            var yRotHeads = witherBoss.yRotHeads;
            var yRotOHeads = witherBoss.yRotOHeads;
            for (var i = 0; i < yRotHeads.length; i++) {
                yRotHeads[i] = HALF_ROT;
                yRotOHeads[i] = HALF_ROT;
            }
        }

        var width = measuringResult.maxX() - measuringResult.minX();
        var height = measuringResult.maxY() - measuringResult.minY();

        var heightScale = SLOT_SIZE / height;
        var widthScale = (SLOT_SIZE / width) * 2;
        var scale = Math.min(widthScale, heightScale);

        poseStack.translate(0, -measuringResult.minY() * heightScale, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(20));
        poseStack.mulPose(Axis.XP.rotationDegrees(5));
        poseStack.scale(scale, scale, -scale);

        RenderSystem.runAsFancy(() -> entityRenderer.render(
            entity, 0, 0, 0, 0, 1, poseStack, guiGraphics.bufferSource(),
            LightTexture.FULL_BRIGHT
        ));
        guiGraphics.flush();

        entityRenderer.setRenderShadow(true);
        Lighting.setupFor3DItems();
        if (scissor) guiGraphics.disableScissor();
    }

    private static void renderCount(Minecraft mc, GuiGraphics guiGraphics, int count) {
        guiGraphics.pose().translate(10, 9, 200);
        guiGraphics.drawString(mc.font, String.valueOf(count), 0, 0, TEXT_COLOR, true);
    }
}

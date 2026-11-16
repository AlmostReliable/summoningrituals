package com.almostreliable.summoningrituals.compat.viewer.common;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.client.util.MeasuringBufferSource;
import com.almostreliable.summoningrituals.client.util.MeasuringBufferSource.MeasuringResult;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.Items;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import org.joml.Vector2f;
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

    private static final Map<Identifier, MeasuringResult> MEASURING_RESULT_CACHE = new HashMap<>();
    private static final int TEXT_COLOR = -1;
    private static final int MEASURE_TICKS = 40;
    private static final int HALF_ROT = 180;

    private EntityIngredientRenderer() {}

    @SuppressWarnings("BooleanParameter")
    public static void render(GuiGraphicsExtractor guiGraphics, EntityIngredient entityIngredient, boolean scissor, boolean count) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !(entityIngredient.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        var measuringResult = measureEntity(mc, entity, entityIngredient.getId());
        if (measuringResult == MeasuringResult.EMPTY) {
            guiGraphics.item(Items.BARRIER.getDefaultInstance(), 0, 0);
            return;
        }

        entity.tickCount = mc.player.tickCount;

        renderEntity(mc, guiGraphics, entity, measuringResult, scissor);

        var entityCount = entityIngredient.getEntityInfo().count();
        if (!count || entityCount <= 1) return;
        var pose = guiGraphics.pose();
        pose.pushMatrix();
        {
            renderCount(mc, guiGraphics, entityCount);
        }
        pose.popMatrix();
    }

    private static MeasuringResult measureEntity(Minecraft mc, LivingEntity entity, Identifier entityId) {
        var cached = MEASURING_RESULT_CACHE.get(entityId);
        if (cached != null) return cached;

        var entityRenderer = mc.getEntityRenderDispatcher();
        var measuringBuffer = new MeasuringBufferSource();
        var cameraRenderState = new CameraRenderState();
        var poseStack = new PoseStack();

        // measure for 40 render ticks because size can change with animation
        try {
            for (var i = 0; i < MEASURE_TICKS; i++) {
                var renderState = entityRenderer.extractEntity(entity, i);
                entityRenderer.submit(renderState, cameraRenderState, 0, 0, 0, poseStack, measuringBuffer);
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

    private static void renderEntity(
        Minecraft mc, GuiGraphicsExtractor guiGraphics, LivingEntity entity, MeasuringResult measuringResult, boolean scissor
    ) {
        if (scissor) {
            guiGraphics.enableScissor(0, 0, SLOT_SIZE, SLOT_SIZE);
        }

        // entity rendering takes absolute coordinates while scissor applies the pose itself
        var absolutePos = guiGraphics.pose().transformPosition(0, 0, new Vector2f());
        var absX = (int) absolutePos.x;
        var absY = (int) absolutePos.y;
        var overflow = scissor ? 0 : SLOT_SIZE;

        var entityRenderer = mc.getEntityRenderDispatcher();

        // need to turn all mobs to the other side because the dragon and the bat have reversed models
        // this requires inverting the models later by a negative z-value;
        // tried so many other approaches, this is the only thing that works
        entity.absSnapRotationTo(HALF_ROT, 0);
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

        var renderState = entityRenderer.extractEntity(entity, 1);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;

        var translation = new Vector3f(0, (SLOT_SIZE / 2f + measuringResult.minY() * heightScale) / scale, 0);
        var rotation = new Quaternionf()
            .rotateZ((float) Math.toRadians(HALF_ROT))
            .rotateY((float) Math.toRadians(-20))
            .rotateX((float) Math.toRadians(-5));
        guiGraphics.entity(
            renderState, scale, translation, rotation, null,
            absX - overflow, absY - overflow, absX + SLOT_SIZE + overflow, absY + SLOT_SIZE + overflow
        );

        if (scissor) guiGraphics.disableScissor();
    }

    private static void renderCount(Minecraft mc, GuiGraphicsExtractor guiGraphics, int count) {
        guiGraphics.pose().translate(10, 9);
        guiGraphics.text(mc.font, String.valueOf(count), 0, 0, TEXT_COLOR, true);
    }
}

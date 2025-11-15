package com.almostreliable.summoningrituals.compat.viewer.jei.entity;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.client.MeasuringBufferSource;
import com.almostreliable.summoningrituals.client.MeasuringBufferSource.MeasuringResult;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.TooltipFlag;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mezz.jei.api.ingredients.IIngredientRenderer;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A renderer to handle rendering entities with a similar size and proper offset to fit a slot
 * bounding box. The different instances have options to hide the rendering of the ingredient count,
 * as well as optional clipping to predefined slot bounds.
 * <p>
 * The renderer makes use of the {@link MeasuringBufferSource} to measure the entity vertices.
 * <p>
 * license: Unlicense
 * <p>
 * Check the README license section for more information.
 */
public final class EntityIngredientRenderer implements IIngredientRenderer<EntityIngredient> {

    public static final EntityIngredientRenderer BOOKMARK_RENDERER = new EntityIngredientRenderer(true, false);
    public static final EntityIngredientRenderer INPUT_RENDERER = new EntityIngredientRenderer(false, true);
    public static final EntityIngredientRenderer OUTPUT_RENDERER = new EntityIngredientRenderer(true, true);
    private static final int TEXT_COLOR = 16_777_215;
    private static final int MEASURE_TICKS = 40;
    private static final int HALF_ROT = 180;
    private static final int SLOT_SIZE = 16;

    private final Minecraft mc = Minecraft.getInstance();
    private final Map<String, MeasuringResult> measuringResultCache = new HashMap<>();
    private final boolean scissor;
    private final boolean renderCount;

    private EntityIngredientRenderer(boolean scissor, boolean renderCount) {
        this.scissor = scissor;
        this.renderCount = renderCount;
    }

    @Override
    public void render(GuiGraphics guiGraphics, EntityIngredient entityIngredient) {
        if (mc.level == null || mc.player == null || !(entityIngredient.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        var entityId = entityIngredient.getResourceLocation().toString();
        var measuringResult = measureEntity(entity, entityId);
        if (measuringResult == MeasuringResult.EMPTY) return;

        entity.tickCount = mc.player.tickCount;

        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        {
            renderEntity(guiGraphics, poseStack, entity, measuringResult);
        }
        poseStack.popPose();

        if (!renderCount) return;
        poseStack.pushPose();
        {
            renderCount(guiGraphics, entityIngredient, poseStack);
        }
        poseStack.popPose();
    }

    @SuppressWarnings("deprecation")
    private MeasuringResult measureEntity(LivingEntity entity, String entityId) {
        var cached = measuringResultCache.get(entityId);
        if (cached != null) return cached;

        var entityRenderer = mc.getEntityRenderDispatcher();
        var measuringBuffer = new MeasuringBufferSource();
        var poseStack = new PoseStack();

        // measure for 40 render ticks because size can change with animation
        for (var i = 0; i < MEASURE_TICKS; i++) {
            var ticks = i;
            RenderSystem.runAsFancy(() -> entityRenderer.render(
                entity, 0, 0, 0, 0, ticks, poseStack, measuringBuffer, LightTexture.FULL_BRIGHT
            ));
        }

        var measuringResult = measuringBuffer.getData();
        if (measuringResult == null) {
            SummoningRituals.LOGGER.error("failed to measure entity: {}", entityId);
            return MeasuringResult.EMPTY;
        }
        measuringResultCache.put(entityId, measuringResult);
        return measuringResult;
    }

    @SuppressWarnings("deprecation")
    private void renderEntity(
        GuiGraphics guiGraphics, PoseStack poseStack, LivingEntity entity, MeasuringResult measuringResult
    ) {
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

    private void renderCount(GuiGraphics guiGraphics, EntityIngredient entityIngredient, PoseStack poseStack) {
        var count = entityIngredient.getEntityInfo().count();
        if (count <= 1) return;
        poseStack.translate(10, 9, 200);
        guiGraphics.drawString(mc.font, String.valueOf(count), 0, 0, TEXT_COLOR, true);
    }

    @Override
    public List<Component> getTooltip(EntityIngredient entity, TooltipFlag tooltipFlag) {
        var tooltip = new ArrayList<Component>();
        tooltip.add(entity.getDisplayName());

        var entityTooltip = entity.getEntityInfo().tooltip();
        if (renderCount && !entityTooltip.isEmpty()) {
            tooltip.addAll(entityTooltip);
        }

        if (tooltipFlag.isAdvanced()) {
            var id = Component.literal(entity.getResourceLocation().toString());
            tooltip.add(id.withStyle(ChatFormatting.DARK_GRAY));
        }
        return tooltip;
    }
}

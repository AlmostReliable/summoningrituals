package com.almostreliable.summoningrituals.compat.viewer.emi.entity;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.client.MeasuringBufferSource;
import com.almostreliable.summoningrituals.compat.viewer.jei.entity.EntityIngredient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiStack;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EntityEmiStack extends EmiStack {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final Map<String, MeasuringBufferSource.MeasuringResult> MEASURING_RESULT_CACHE = new HashMap<>();
    private static final int TEXT_COLOR = 16_777_215;
    private static final int MEASURE_TICKS = 40;
    private static final int HALF_ROT = 180;
    private static final int SLOT_SIZE = 16;

    private final EntityIngredient entityIngredient;
    private final boolean scissor;

    private EntityEmiStack(EntityIngredient entityIngredient, boolean scissor) {
        this.entityIngredient = entityIngredient;
        this.scissor = scissor;
        this.amount = entityIngredient.getEntityInfo().count();
    }

    public static EntityEmiStack input(EntityIngredient entityIngredient) {
        return new EntityEmiStack(entityIngredient, false);
    }

    public static EntityEmiStack output(EntityIngredient entityIngredient) {
        return new EntityEmiStack(entityIngredient, true);
    }

    @Override
    public EmiStack copy() {
        return new EntityEmiStack(entityIngredient.copy(), scissor);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float delta, int flags) {
        if (MC.level == null || MC.player == null || !(entityIngredient.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        var entityId = entityIngredient.getResourceLocation().toString();
        var measuringResult = measureEntity(entity, entityId);
        if (measuringResult == MeasuringBufferSource.MeasuringResult.EMPTY) return;

        entity.tickCount = MC.player.tickCount;

        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        {
            poseStack.translate(x, y, 0);

            poseStack.pushPose();
            {
                var respectScissor = flags == -1; // in recipe display, flags is always -1 (distinction between display and tree view)
                renderEntity(guiGraphics, poseStack, entity, measuringResult, respectScissor);
            }
            poseStack.popPose();

            if ((flags & RENDER_AMOUNT) != 0) {
                poseStack.pushPose();
                {
                    renderCount(guiGraphics, entityIngredient, poseStack);
                }
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }

    @SuppressWarnings("deprecation")
    private MeasuringBufferSource.MeasuringResult measureEntity(LivingEntity entity, String entityId) {
        var cached = MEASURING_RESULT_CACHE.get(entityId);
        if (cached != null) return cached;

        var entityRenderer = MC.getEntityRenderDispatcher();
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
            return MeasuringBufferSource.MeasuringResult.EMPTY;
        }
        MEASURING_RESULT_CACHE.put(entityId, measuringResult);
        return measuringResult;
    }

    @SuppressWarnings("deprecation")
    private void renderEntity(
        GuiGraphics guiGraphics, PoseStack poseStack, LivingEntity entity, MeasuringBufferSource.MeasuringResult measuringResult,
        boolean respectScissor
    ) {
        if (respectScissor && scissor) {
            // gui graphics scissor doesn't take pose stack into account, bruh
            var absolutePos = poseStack.last().pose().transformPosition(0, 0, 0, new Vector3f());
            var absX = (int) absolutePos.x;
            var absY = (int) absolutePos.y;
            guiGraphics.enableScissor(absX, absY, absX + SLOT_SIZE, absY + SLOT_SIZE);
        }

        poseStack.translate(SLOT_SIZE / 2f, SLOT_SIZE, 100);
        poseStack.mulPose(Axis.ZP.rotationDegrees(HALF_ROT));

        Lighting.setupForEntityInInventory();
        var entityRenderer = MC.getEntityRenderDispatcher();
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
        if (respectScissor && scissor) guiGraphics.disableScissor();
    }

    private void renderCount(GuiGraphics guiGraphics, EntityIngredient entityIngredient, PoseStack poseStack) {
        var count = entityIngredient.getEntityInfo().count();
        if (count <= 1) return;
        poseStack.translate(10, 9, 200);
        guiGraphics.drawString(MC.font, String.valueOf(count), 0, 0, TEXT_COLOR, true);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public DataComponentPatch getComponentChanges() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    public Object getKey() {
        return entityIngredient.getEntityInfo().entity().value();
    }

    @Override
    public ResourceLocation getId() {
        return entityIngredient.getResourceLocation();
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of(); // not required, entity stacks are not added to index, this is only for search
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        var tooltips = entityIngredient.getTooltip(true, MC.options.advancedItemTooltips);
        var res = new ArrayList<ClientTooltipComponent>();
        for (var tooltip : tooltips) {
            res.add(EmiTooltipComponents.of(tooltip));
        }
        EmiTooltipComponents.appendModName(res, getId().getNamespace());
        return res;
    }

    @Override
    public Component getName() {
        return entityIngredient.getDisplayName();
    }
}

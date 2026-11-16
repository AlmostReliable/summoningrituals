package com.almostreliable.summoningrituals.client.render;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.recipe.container.RecipeInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess") // exposed for custom KubeJS renderers
public record AltarRenderer(ItemModelResolver getItemModelResolver) implements BlockEntityRenderer<AltarBlockEntity, AltarRenderer.State> {

    public static final Map<Identifier, CustomRitualRenderer> CUSTOM_RENDERERS = new HashMap<>();
    public static final float HALF = .5f;
    public static final float HALF_CIRCLE = 180f;
    public static final float FULL_CIRCLE = 360f;
    public static final float ALTAR_RENDER_HEIGHT = 0.8f;
    private static final int MAX_ITEM_HEIGHT = 2;
    private static final int RESET_TICKS = 20;
    private static final float ORBIT_DEGREES_PER_SECOND = 20f;
    private static final float MAX_PROGRESS_HEIGHT = 2.5f;
    private static final float ITEM_OFFSET = 1.5f;

    public AltarRenderer(Context context) {
        this(context.itemModelResolver());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        AltarBlockEntity altar,
        State state,
        float partialTick,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(altar, state, partialTick, cameraPosition, breakProgress);
        state.altar = null;

        var player = Minecraft.getInstance().player;
        var level = altar.getLevel();
        var altarPos = altar.getBlockPos();
        var renderDistance = Config.CLIENT.inventoryRenderDistance.getAsInt();
        if (player == null || level == null || !altarPos.closerThan(player.blockPosition(), renderDistance)) return;

        var altarCenterPos = Vec3.atCenterOf(altarPos);
        var playerPos = new Vec3(
            Mth.lerp(partialTick, player.xOld, player.getX()),
            Mth.lerp(partialTick, player.yOld, player.getY()),
            Mth.lerp(partialTick, player.zOld, player.getZ())
        );
        var playerToAltarRatio = Math.atan2(altarCenterPos.x - playerPos.x, playerPos.z - altarCenterPos.z);

        var inputs = new ArrayList<ItemStackRenderState>();

        for (var input : altar.getInventory().getDisplayItems()) {
            var inputRenderState = extractItem(input, level);
            inputs.add(inputRenderState);
        }

        state.altar = altar;
        state.recipeInfo = altar.getCurrentRecipeInfo();
        state.initiator = extractItem(altar.getInventory().getInitiator(), level);
        state.inputs = inputs;
        state.playerToAltarDistance = (float) altarCenterPos.distanceTo(playerPos);
        state.playerToAltarAngle = (float) (Math.toDegrees(playerToAltarRatio) + HALF_CIRCLE);
        state.recipeProgress = altar.getRecipeProgress();
        state.recipeTime = altar.getRecipeTime();
        state.lightAbove = LevelRenderer.getLightCoords(level, altarPos.above());
        state.partialTick = partialTick;
    }

    private ItemStackRenderState extractItem(ItemStack stack, Level level) {
        var itemRenderState = new ItemStackRenderState();
        getItemModelResolver.updateForTopItem(itemRenderState, stack, ItemDisplayContext.FIXED, level, null, 0);

        return itemRenderState;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        var altar = state.altar;
        if (altar == null) return;

        var player = Minecraft.getInstance().player;
        var level = altar.getLevel();
        if (player == null || level == null) return;

        CustomRitualRenderer customRenderer = null;
        var recipeInfo = state.recipeInfo;
        if (recipeInfo != null) {
            customRenderer = CUSTOM_RENDERERS.get(recipeInfo.getRecipeId());
        }

        var renderContext = new AltarRenderContext(
            level,
            player,
            altar,
            state.playerToAltarDistance,
            state.playerToAltarAngle,
            state.recipeProgress,
            state.recipeTime,
            poseStack,
            submitNodeCollector,
            state.lightAbove,
            state.partialTick,
            state.initiator,
            state.inputs
        );

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

    private void renderInventoryContents(AltarRenderContext renderContext) {
        renderContext.translate(0, MAX_PROGRESS_HEIGHT * renderContext.getRecipeProgressRatio(), 0);

        renderInitiator(renderContext);
        renderItemOrbit(renderContext);

        if (renderContext.shouldReset()) {
            renderContext.getAltar().resetStartTick = renderContext.getGameTime();
        }
    }

    public void renderInitiator(AltarRenderContext renderContext) {
        var initiator = renderContext.getInitiatorRenderState();
        if (initiator.isEmpty()) return;

        renderContext.pushPose();
        {
            renderContext.translate(0, invert(0.75f * renderContext.getRecipeProgressRatio()), 0);
            renderContext.scale(0.75f);
            renderContext.mulPose(Axis.YN.rotationDegrees(renderContext.getPlayerToAltarAngle()));
            renderContext.renderItem(initiator);
        }
        renderContext.popPose();
    }

    public void renderItemOrbit(AltarRenderContext renderContext) {
        var inputs = renderContext.getInputRenderStates();
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

                renderContext.renderItem(inputs.get(i));
            }
            renderContext.popPose();
        }
    }

    public float calculateOrbitRotation() {
        var minecraft = Minecraft.getInstance();
        var renderTicks = minecraft.levelRenderer.getTicks()
            + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        var renderSeconds = renderTicks / 20f;
        return clampRotation(ORBIT_DEGREES_PER_SECOND * renderSeconds);
    }

    public static float clampRotation(float degree) {
        return ((degree % FULL_CIRCLE) + FULL_CIRCLE) % FULL_CIRCLE;
    }

    public static float invert(float value) {
        return 1 - value;
    }

    public static class State extends BlockEntityRenderState {

        @Nullable
        private AltarBlockEntity altar;
        @Nullable
        private RecipeInfo recipeInfo;
        private ItemStackRenderState initiator = new ItemStackRenderState();
        private List<ItemStackRenderState> inputs = List.of();
        private float playerToAltarDistance;
        private float playerToAltarAngle;
        private int recipeProgress;
        private int recipeTime;
        private int lightAbove;
        private float partialTick;
    }
}

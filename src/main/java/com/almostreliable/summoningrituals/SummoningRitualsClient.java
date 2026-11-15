package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.client.render.AltarRenderer;
import com.almostreliable.summoningrituals.client.render.BlockHighlightRenderer;
import com.almostreliable.summoningrituals.client.render.BlockPatternRenderer;
import com.almostreliable.summoningrituals.client.tooltip.PatternPreviewTooltipComponent;
import com.almostreliable.summoningrituals.compat.kubejs.KubeFacade;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.dev.PatternGeneratorItem;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

import org.jetbrains.annotations.Nullable;

@Mod(value = ModConstants.MOD_ID, dist = Dist.CLIENT)
public final class SummoningRitualsClient {

    @Nullable
    private static FeatureRenderDispatcher overlayFeatureRenderer;

    public SummoningRitualsClient(IEventBus eventBus) {
        eventBus.addListener(SummoningRitualsClient::registerEntityRenderers);
        eventBus.addListener(SummoningRitualsClient::registerTooltipRenderers);
        eventBus.addListener(SummoningRitualsClient::registerClientReloadListeners);
        eventBus.addListener(SummoningRitualsClient::registerRenderPipelines);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onRenderLevelStage);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onRecipesReceived);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onLoggingOut);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.ALTAR_BLOCK_ENTITY.get(), AltarRenderer::new);
    }

    private static void registerTooltipRenderers(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(PatternPreviewTooltipComponent.Data.class, PatternPreviewTooltipComponent::new);
    }

    private static void registerClientReloadListeners(AddClientReloadListenersEvent event) {
        KubeFacade.registerReloadListener(event);
    }

    private static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(BlockHighlightRenderer.XRAY_LINES_PIPELINE);
    }

    private static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        var mc = Minecraft.getInstance();
        var featureRenderer = getOverlayFeatureRenderer(mc);
        var poseStack = event.getPoseStack();
        var submitNodeCollector = featureRenderer.getSubmitNodeStorage();
        var camera = event.getLevelRenderState().cameraRenderState;
        BlockPatternRenderer.INSTANCE.render(poseStack, submitNodeCollector, camera);
        BlockHighlightRenderer.INSTANCE.render(poseStack, submitNodeCollector, camera.pos);
        featureRenderer.renderAllFeatures();
        mc.renderBuffers().bufferSource().endBatch();
    }

    private static FeatureRenderDispatcher getOverlayFeatureRenderer(Minecraft mc) {
        if (overlayFeatureRenderer == null) {
            var renderBuffers = mc.renderBuffers();
            overlayFeatureRenderer = new FeatureRenderDispatcher(
                new SubmitNodeStorage(),
                mc.getModelManager(),
                renderBuffers.bufferSource(),
                mc.getAtlasManager(),
                renderBuffers.outlineBufferSource(),
                renderBuffers.crumblingBufferSource(),
                mc.font,
                mc.gameRenderer.getGameRenderState()
            );
        }
        return overlayFeatureRenderer;
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        BlockPatternRenderer.clear();
        BlockHighlightRenderer.clear();
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null || mc.isPaused()) return;

        var inventory = player.getInventory();
        for (var slot = 0; slot < inventory.getContainerSize(); slot++) {
            var stack = inventory.getItem(slot);
            if (stack.getItem() instanceof PatternGeneratorItem patternGenerator) {
                patternGenerator.inventoryTick(stack, player.level());
            }
        }
    }

    private static void onRecipesReceived(RecipesReceivedEvent event) {
        AltarRecipe.setClientRecipeMap(event.getRecipeMap());
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        AltarRecipe.setClientRecipeMap(RecipeMap.EMPTY);
    }

    public static RecipeMap getRecipeMap() {
        return AltarRecipe.getClientRecipeMap();
    }
}

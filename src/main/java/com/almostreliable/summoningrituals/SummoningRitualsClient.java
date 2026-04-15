package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.client.render.AltarRenderer;
import com.almostreliable.summoningrituals.client.render.BlockHighlightRenderer;
import com.almostreliable.summoningrituals.client.render.PatternPreviewRenderer;
import com.almostreliable.summoningrituals.client.tooltip.PatternPreviewTooltipComponent;
import com.almostreliable.summoningrituals.compat.kubejs.event.RitualRendererRegistryEvent;
import com.almostreliable.summoningrituals.core.Registration;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

@Mod(value = ModConstants.MOD_ID, dist = Dist.CLIENT)
public final class SummoningRitualsClient {

    public SummoningRitualsClient(IEventBus eventBus) {
        eventBus.addListener(SummoningRitualsClient::registerEntityRenderers);
        eventBus.addListener(SummoningRitualsClient::registerTooltipRenderers);
        eventBus.addListener(SummoningRitualsClient::registerClientReloadListeners);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onRenderLevelStage);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onLevelUnload);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.ALTAR_BLOCK_ENTITY.get(), AltarRenderer::new);
    }

    private static void registerTooltipRenderers(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(PatternPreviewTooltipComponent.Data.class, PatternPreviewTooltipComponent::new);
    }

    private static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new RitualRendererRegistryEvent.ReloadListener());
    }

    private static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        var poseStack = event.getPoseStack();
        var camera = event.getCamera();
        PatternPreviewRenderer.INSTANCE.render(poseStack, camera);
        BlockHighlightRenderer.INSTANCE.render(poseStack, camera);
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        PatternPreviewRenderer.clear();
        BlockHighlightRenderer.clear();
    }
}

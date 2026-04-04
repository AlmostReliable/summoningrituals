package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.client.render.AltarRenderer;
import com.almostreliable.summoningrituals.client.render.PatternPreviewRenderer;
import com.almostreliable.summoningrituals.client.tooltip.PatternPreviewTooltipComponent;
import com.almostreliable.summoningrituals.core.Registration;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ModConstants.MOD_ID, dist = Dist.CLIENT)
public final class SummoningRitualsClient {

    public SummoningRitualsClient(IEventBus eventBus) {
        eventBus.addListener(SummoningRitualsClient::registerRenderers);
        eventBus.addListener(SummoningRitualsClient::registerTooltipRenderers);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onRenderLevelStage);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.ALTAR_BLOCK_ENTITY.get(), AltarRenderer::new);
    }

    private static void registerTooltipRenderers(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(PatternPreviewTooltipComponent.Data.class, PatternPreviewTooltipComponent::new);
    }

    private static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) return;
        PatternPreviewRenderer.INSTANCE.render(event.getPoseStack(), event.getCamera());
    }
}

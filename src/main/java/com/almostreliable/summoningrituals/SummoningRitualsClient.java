package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.client.render.AltarRenderer;
import com.almostreliable.summoningrituals.client.render.BlockPatternRenderer;
import com.almostreliable.summoningrituals.client.tooltip.PatternPreviewTooltipComponent;
import com.almostreliable.summoningrituals.compat.kubejs.KubeFacade;
import com.almostreliable.summoningrituals.core.ModRegistration;
import com.almostreliable.summoningrituals.dev.PatternGeneratorItem;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

@Mod(value = ModConstants.MOD_ID, dist = Dist.CLIENT)
public final class SummoningRitualsClient {

    private static RecipeMap recipeMap = RecipeMap.EMPTY;

    public SummoningRitualsClient(IEventBus eventBus) {
        eventBus.addListener(SummoningRitualsClient::registerEntityRenderers);
        eventBus.addListener(SummoningRitualsClient::registerTooltipRenderers);
        eventBus.addListener(SummoningRitualsClient::registerClientReloadListeners);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onExtractLevelRenderState);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onSubmitCustomGeometry);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onRecipesReceived);
        NeoForge.EVENT_BUS.addListener(SummoningRitualsClient::onLoggingOut);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModRegistration.ALTAR_BLOCK_ENTITY.get(), AltarRenderer::new);
    }

    private static void registerTooltipRenderers(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(PatternPreviewTooltipComponent.Data.class, PatternPreviewTooltipComponent::new);
    }

    private static void registerClientReloadListeners(AddClientReloadListenersEvent event) {
        KubeFacade.registerReloadListener(event);
    }

    private static void onExtractLevelRenderState(ExtractLevelRenderStateEvent event) {
        var partialTick = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        BlockPatternRenderer.INSTANCE.extractRenderState(event.getRenderState(), partialTick);
    }

    private static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        BlockPatternRenderer.submit(event.getLevelRenderState(), event.getPoseStack(), event.getSubmitNodeCollector());
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        BlockPatternRenderer.clear();
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        BlockPatternRenderer.INSTANCE.tick();

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var mainHandStack = player.getMainHandItem();
        if (mainHandStack.getItem() instanceof PatternGeneratorItem patternGenerator) {
            patternGenerator.clientTick(mainHandStack);
        }
    }

    private static void onRecipesReceived(RecipesReceivedEvent event) {
        recipeMap = event.getRecipeMap();
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        recipeMap = RecipeMap.EMPTY;
    }

    public static RecipeMap getRecipeMap() {
        return recipeMap;
    }
}

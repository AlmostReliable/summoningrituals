package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.client.render.AltarRenderer;
import com.almostreliable.summoningrituals.client.render.BlockPatternRenderer;
import com.almostreliable.summoningrituals.client.tooltip.PatternPreviewTooltipComponent;
import com.almostreliable.summoningrituals.compat.kubegroovy.KubeGroovyFacade;
import com.almostreliable.summoningrituals.compat.kubejs.KubeJsFacade;
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
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ModConstants.MOD_ID, dist = Dist.CLIENT)
public final class SummoningRitualsClient {

    private static RecipeMap RECIPE_MAP = RecipeMap.EMPTY;

    public SummoningRitualsClient(IEventBus eventBus) {
        eventBus.addListener(SummoningRitualsClient::registerEntityRenderers);
        eventBus.addListener(SummoningRitualsClient::registerTooltipRenderers);
        eventBus.addListener(SummoningRitualsClient::registerClientReloadListeners);
        BlockPatternRenderer.init(NeoForge.EVENT_BUS);
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
        KubeJsFacade.registerReloadListener(event);
        KubeGroovyFacade.registerReloadListener(event);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var mainHandStack = player.getMainHandItem();
        if (mainHandStack.getItem() instanceof PatternGeneratorItem patternGenerator) {
            patternGenerator.clientTick(mainHandStack);
        }
    }

    private static void onRecipesReceived(RecipesReceivedEvent event) {
        RECIPE_MAP = event.getRecipeMap();
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        RECIPE_MAP = RecipeMap.EMPTY;
    }

    public static RecipeMap getRecipeMap() {
        return RECIPE_MAP;
    }
}

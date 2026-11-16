package com.almostreliable.summoningrituals.compat.kubegroovy;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubegroovy.event.KubeEvents;
import com.almostreliable.summoningrituals.compat.kubegroovy.event.ModifyConditionsTooltipEvent;
import com.almostreliable.summoningrituals.compat.kubegroovy.event.RitualRendererRegistryKubeEvent;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

import java.util.List;

public final class KubeGroovyFacade {

    private KubeGroovyFacade() {}

    public static void registerReloadListener(AddClientReloadListenersEvent event) {
        if (notLoaded()) return;

        var reloadListener = new RitualRendererRegistryKubeEvent.ReloadListener();
        event.addListener(SummoningRituals.getRL("ritual_renderers"), reloadListener);
    }

    public static void postModifyConditionsTooltipEvent(Identifier recipeId, AltarRecipe recipe, List<Component> tooltip) {
        if (notLoaded()) return;
        Facade.postModifyConditionsTooltipEvent(recipeId, recipe, tooltip);
    }

    private static boolean notLoaded() {
        var modId = "kubegroovy";
        var modList = ModList.get();
        if (modList == null) {
            return FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) == null;
        }
        return !modList.isLoaded(modId);
    }

    private static final class Facade {

        private static void postModifyConditionsTooltipEvent(Identifier recipeId, AltarRecipe recipe, List<Component> tooltip) {
            if (!KubeEvents.MODIFY_CONDITIONS_TOOLTIP.hasListeners()) return;
            KubeEvents.MODIFY_CONDITIONS_TOOLTIP.post(new ModifyConditionsTooltipEvent(recipeId, recipe, tooltip));
        }
    }
}

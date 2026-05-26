package com.almostreliable.summoningrituals.compat.kubejs;

import com.almostreliable.summoningrituals.compat.kubejs.event.KubeEvents;
import com.almostreliable.summoningrituals.compat.kubejs.event.ModifyConditionsTooltipEvent;
import com.almostreliable.summoningrituals.compat.kubejs.event.RitualRendererRegistryKubeEvent;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

import java.util.List;

public final class KubeFacade {

    private KubeFacade() {}

    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        if (notLoaded()) return;
        event.registerReloadListener(new RitualRendererRegistryKubeEvent.ReloadListener());
    }

    public static void postModifyConditionsTooltipEvent(ResourceLocation recipeId, AltarRecipe recipe, List<Component> tooltip) {
        if (notLoaded()) return;
        Facade.postModifyConditionsTooltipEvent(recipeId, recipe, tooltip);
    }

    private static boolean notLoaded() {
        var modId = "kubejs";
        var modList = ModList.get();
        if (modList == null) {
            return LoadingModList.get().getMods().stream().map(ModInfo::getModId).noneMatch(modId::equals);
        }
        return !modList.isLoaded(modId);
    }

    private static final class Facade {

        private static void postModifyConditionsTooltipEvent(ResourceLocation recipeId, AltarRecipe recipe, List<Component> tooltip) {
            if (!KubeEvents.MODIFY_CONDITIONS_TOOLTIP.hasListeners()) return;
            KubeEvents.MODIFY_CONDITIONS_TOOLTIP.post(new ModifyConditionsTooltipEvent(recipeId, recipe, tooltip));
        }
    }
}

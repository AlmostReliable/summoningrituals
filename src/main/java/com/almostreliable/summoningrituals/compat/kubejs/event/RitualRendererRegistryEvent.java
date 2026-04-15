package com.almostreliable.summoningrituals.compat.kubejs.event;

import com.almostreliable.summoningrituals.client.render.AltarRenderer;
import com.almostreliable.summoningrituals.client.render.BlockPatternRenderer;
import com.almostreliable.summoningrituals.client.render.CustomRitualRenderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.HashMap;
import java.util.Map;

public final class RitualRendererRegistryEvent implements KubeEvent {

    private RitualRendererRegistryEvent() {}

    private final Map<ResourceLocation, CustomRitualRenderer> renderers = new HashMap<>();

    public void register(ResourceLocation id, CustomRitualRenderer renderer) {
        renderers.put(id, renderer);
    }

    @HideFromJS
    public static final class ReloadListener implements ResourceManagerReloadListener {

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            AltarRenderer.CUSTOM_RENDERERS.clear();
            BlockPatternRenderer.clear();
            if (KubeEvents.RITUAL_RENDERER_REGISTRY.hasListeners()) {
                var event = new RitualRendererRegistryEvent();
                KubeEvents.RITUAL_RENDERER_REGISTRY.post(event);
                AltarRenderer.CUSTOM_RENDERERS.putAll(event.renderers);
            }
        }
    }
}

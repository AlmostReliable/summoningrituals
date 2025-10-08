package com.almostreliable.summoningrituals.core;

import com.almostreliable.summoningrituals.recipe.AltarRecipe;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public final class ReloadListener implements ResourceManagerReloadListener {

    public static final ReloadListener INSTANCE = new ReloadListener();

    private ReloadListener() {}

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        AltarRecipe.clearCaches();
    }
}

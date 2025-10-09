package com.almostreliable.summoningrituals.mixin;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.fml.loading.FMLEnvironment;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void summoning$onReload(
        Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci
    ) {
        AltarRecipe.clearCaches();
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    private void summoning$postReload(
        Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci
    ) {
        if (FMLEnvironment.production) return;

        SummoningRituals.LOGGER.info("========== Added Recipes ==========");
        recipes.keySet().stream().filter(id -> id.getNamespace().equals("summoningrituals")).forEach(id -> {
            SummoningRituals.LOGGER.info("> {}", id);
            SummoningRituals.LOGGER.info(recipes.get(id).toString());
        });
    }
}

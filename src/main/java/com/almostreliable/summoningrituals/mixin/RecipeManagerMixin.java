package com.almostreliable.summoningrituals.mixin;

import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@SuppressWarnings("NewMethodNamingConvention")
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void summoning$apply(
        Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profilerFiller,
        CallbackInfo ci
    ) {
        AltarRecipe.CATALYST_CACHE.clear();
    }
}

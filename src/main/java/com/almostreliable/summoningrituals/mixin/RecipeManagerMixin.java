package com.almostreliable.summoningrituals.mixin;

import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.fml.loading.FMLEnvironment;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Shadow
    @Final
    private HolderLookup.Provider registries;

    @Inject(
        method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)" +
            "Lnet/minecraft/world/item/crafting/RecipeMap;",
        at = @At("HEAD")
    )
    private void summoning$onPrepare(ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfoReturnable<RecipeMap> cir) {
        AltarRecipe.clearPendingValidators();
    }

    @Inject(
        method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;" +
            "Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At("HEAD")
    )
    private void summoning$onReload(RecipeMap recipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        AltarRecipe.clearCaches();
    }

    @Inject(
        method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;" +
            "Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At("TAIL")
    )
    private void summoning$postReload(RecipeMap recipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        if (FMLEnvironment.isProduction()) return;

        SummoningRituals.LOGGER.info("========== Added Recipes ==========");
        recipes.values().stream().filter(holder -> holder.id().identifier().getNamespace().equals(ModConstants.MOD_ID)).forEach(holder -> {
            SummoningRituals.LOGGER.info("> {}", holder.id().identifier());
            var ops = registries.createSerializationContext(JsonOps.INSTANCE);
            Recipe.CODEC.encodeStart(ops, holder.value()).result().map(JsonElement::toString).ifPresent(SummoningRituals.LOGGER::info);
        });
    }
}

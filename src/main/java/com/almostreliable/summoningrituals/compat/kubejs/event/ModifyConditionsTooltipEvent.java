package com.almostreliable.summoningrituals.compat.kubejs.event;

import com.almostreliable.summoningrituals.recipe.AltarRecipe;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.event.KubeEvent;

import java.util.List;

public record ModifyConditionsTooltipEvent(
    ResourceLocation getRecipeId, AltarRecipe getRecipe, List<Component> getTooltip
) implements KubeEvent {}

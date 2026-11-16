package com.almostreliable.summoningrituals.compat.kubegroovy.event;

import com.almostreliable.summoningrituals.recipe.AltarRecipe;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import dev.uncandango.kubegroovy.event.KubeEvent;

import java.util.List;

public record ModifyConditionsTooltipEvent(
    Identifier getRecipeId,
    AltarRecipe getRecipe,
    List<Component> getTooltip
) implements KubeEvent {}

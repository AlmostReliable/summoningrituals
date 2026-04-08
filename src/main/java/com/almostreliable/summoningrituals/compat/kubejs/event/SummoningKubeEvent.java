package com.almostreliable.summoningrituals.compat.kubejs.event;

import com.almostreliable.summoningrituals.recipe.RecipeInfoContainer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import dev.latvian.mods.kubejs.level.KubeLevelEvent;

import org.jetbrains.annotations.Nullable;

public record SummoningKubeEvent(
    ServerLevel getLevel, BlockPos getPos, RecipeInfoContainer getRecipeInfo, @Nullable ServerPlayer getPlayer
) implements KubeLevelEvent {}

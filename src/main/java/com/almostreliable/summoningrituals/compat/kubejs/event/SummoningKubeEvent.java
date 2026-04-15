package com.almostreliable.summoningrituals.compat.kubejs.event;

import com.almostreliable.summoningrituals.recipe.container.RecipeInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import dev.latvian.mods.kubejs.level.KubeLevelEvent;

import org.jetbrains.annotations.Nullable;

public record SummoningKubeEvent(
    ServerLevel getLevel, BlockPos getPos, RecipeInfo getRecipeInfo, @Nullable ServerPlayer getPlayer
) implements KubeLevelEvent {}

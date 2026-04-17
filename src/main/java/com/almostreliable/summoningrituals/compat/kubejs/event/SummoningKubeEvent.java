package com.almostreliable.summoningrituals.compat.kubejs.event;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.recipe.container.RecipeInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.event.KubeEvent;

import org.jetbrains.annotations.Nullable;

public final class SummoningKubeEvent implements KubeEvent {

    private final ServerLevel level;
    private final AltarBlockEntity getAltar;
    private final RecipeInfo getRecipeInfo;
    private final @Nullable ServerPlayer getPlayer;

    public SummoningKubeEvent(AltarBlockEntity getAltar, RecipeInfo getRecipeInfo, @Nullable ServerPlayer getPlayer) {
        Preconditions.checkArgument(getAltar.getLevel() instanceof ServerLevel, "altar must be in a server level");
        this.level = (ServerLevel) getAltar.getLevel();
        this.getAltar = getAltar;
        this.getRecipeInfo = getRecipeInfo;
        this.getPlayer = getPlayer;
    }

    private ServerLevel getLevel() {
        return level;
    }

    public AltarBlockEntity getAltar() {
        return getAltar;
    }

    public RecipeInfo getRecipeInfo() {
        return getRecipeInfo;
    }

    public @Nullable ServerPlayer getPlayer() {
        return getPlayer;
    }
}

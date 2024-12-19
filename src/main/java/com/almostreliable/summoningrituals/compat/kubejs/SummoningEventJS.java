package com.almostreliable.summoningrituals.compat.kubejs;

import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import dev.latvian.mods.kubejs.level.KubeLevelEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class SummoningEventJS implements KubeLevelEvent {

    private final ServerLevel serverLevel;
    private final BlockPos pos;
    private final AltarRecipe recipe;
    @Nullable private final ServerPlayer player;

    SummoningEventJS(ServerLevel serverLevel, BlockPos pos, AltarRecipe recipe, @Nullable ServerPlayer player) {
        this.serverLevel = serverLevel;
        this.pos = pos;
        this.recipe = recipe;
        this.player = player;
    }

    @Override
    public ServerLevel getLevel() {
        return serverLevel;
    }

    public BlockPos getPos() {
        return pos;
    }

    public AltarRecipe getRecipe() {
        return recipe;
    }

    @Nullable
    public ServerPlayer getPlayer() {
        return player;
    }
}

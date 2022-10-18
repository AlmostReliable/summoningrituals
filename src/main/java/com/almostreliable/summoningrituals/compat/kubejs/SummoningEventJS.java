package com.almostreliable.summoningrituals.compat.kubejs;

import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import dev.latvian.mods.kubejs.level.LevelEventJS;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class SummoningEventJS extends LevelEventJS {

    private final ServerLevel level;
    private final BlockPos pos;
    private final AltarRecipe recipe;
    @Nullable private final ServerPlayer player;

    SummoningEventJS(ServerLevel level, BlockPos pos, AltarRecipe recipe, @Nullable ServerPlayer player) {
        this.level = level;
        this.pos = pos;
        this.recipe = recipe;
        this.player = player;
    }

    @Override
    public ServerLevel getLevel() {
        return level;
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

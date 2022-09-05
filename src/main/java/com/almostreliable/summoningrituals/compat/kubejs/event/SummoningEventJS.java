package com.almostreliable.summoningrituals.compat.kubejs.event;

import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import dev.latvian.mods.kubejs.level.LevelEventJS;
import dev.latvian.mods.kubejs.level.LevelJS;
import dev.latvian.mods.kubejs.player.PlayerJS;
import dev.latvian.mods.kubejs.player.ServerPlayerJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class SummoningEventJS extends LevelEventJS {

    private final Level level;
    private final BlockPos pos;
    private final AltarRecipe recipe;
    @Nullable private final ServerPlayer player;

    SummoningEventJS(Level level, BlockPos pos, AltarRecipe recipe, @Nullable ServerPlayer player) {
        this.level = level;
        this.pos = pos;
        this.recipe = recipe;
        this.player = player;
    }

    @Override
    public LevelJS getLevel() {
        return levelOf(level);
    }

    public BlockPos getPos() {
        return pos;
    }

    public AltarRecipe getRecipe() {
        return recipe;
    }

    @Nullable
    public ServerPlayerJS getPlayer() {
        return (ServerPlayerJS) getLevel().getPlayer(player);
    }

    public static class StartSummoningEventJS extends SummoningEventJS {

        StartSummoningEventJS(Level level, BlockPos pos, AltarRecipe recipe, @Nullable ServerPlayer player) {
            super(level, pos, recipe, player);
        }

        @Override
        public boolean canCancel() {
            return true;
        }
    }
}

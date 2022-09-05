package com.almostreliable.summoningrituals.compat.kubejs.event;

import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;

public interface ISummoningObserver {

    @Nullable
    static ISummoningObserver create() {
        if (ModList.get().isLoaded("kubejs")) {
            return new SummoningObserver();
        }
        return null;
    }

    void onSummoningStart(Level level, BlockPos pos, AltarRecipe recipe, @Nullable ServerPlayer player);

    void onSummoningComplete(Level level, BlockPos pos, AltarRecipe recipe, @Nullable ServerPlayer player);
}

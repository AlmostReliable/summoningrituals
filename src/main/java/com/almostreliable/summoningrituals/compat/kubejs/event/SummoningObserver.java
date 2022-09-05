package com.almostreliable.summoningrituals.compat.kubejs.event;

import com.almostreliable.summoningrituals.altar.AltarEntity;
import com.almostreliable.summoningrituals.compat.kubejs.event.SummoningEventJS.StartSummoningEventJS;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class SummoningObserver implements ISummoningObserver {

    @Override
    public void onSummoningStart(Level level, BlockPos pos, AltarRecipe recipe, @Nullable ServerPlayer player) {
        var event = new StartSummoningEventJS(level, pos, recipe, player);
        event.post(ScriptType.SERVER, "summoningrituals.start");
        if (event.isCancelled() && level.getBlockEntity(pos) instanceof AltarEntity altar) {
            altar.resetSummoning(true);
        }
    }

    @Override
    public void onSummoningComplete(Level level, BlockPos pos, AltarRecipe recipe, @Nullable ServerPlayer player) {
        new SummoningEventJS(level, pos, recipe, player).post(ScriptType.SERVER, "summoningrituals.complete");
    }
}

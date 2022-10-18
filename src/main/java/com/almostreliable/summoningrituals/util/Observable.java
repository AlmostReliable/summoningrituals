package com.almostreliable.summoningrituals.util;

import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Observable {

    private final List<Observer> observers = new ArrayList<>();

    public boolean invoke(ServerLevel level, BlockPos pos, AltarRecipe recipe, @Nullable ServerPlayer player) {
        for (var o : observers) {
            if (o.run(level, pos, recipe, player)) return false;
        }
        return true;
    }

    public void register(Observer observer) {
        observers.add(observer);
    }

    @FunctionalInterface
    public interface Observer {
        boolean run(ServerLevel level, BlockPos pos, AltarRecipe recipe, @Nullable ServerPlayer player);
    }
}

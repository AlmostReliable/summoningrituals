package com.almostreliable.summoningrituals.compat;

import com.almostreliable.summoningrituals.recipe.container.RecipeInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AltarObservable {

    private final List<Observer> observers = new ArrayList<>();

    public boolean invoke(ServerLevel level, BlockPos pos, RecipeInfo recipeInfo, @Nullable ServerPlayer player) {
        for (var o : observers) {
            if (o.run(level, pos, recipeInfo, player)) return false;
        }
        return true;
    }

    public void register(Observer observer) {
        observers.add(observer);
    }

    @FunctionalInterface
    public interface Observer {

        boolean run(ServerLevel level, BlockPos pos, RecipeInfo recipeInfo, @Nullable ServerPlayer player);
    }
}

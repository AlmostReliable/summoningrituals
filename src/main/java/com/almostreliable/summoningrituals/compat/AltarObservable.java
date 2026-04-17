package com.almostreliable.summoningrituals.compat;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.recipe.container.RecipeInfo;

import net.minecraft.server.level.ServerPlayer;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AltarObservable {

    private final List<Observer> observers = new ArrayList<>();

    public boolean invoke(AltarBlockEntity altar, RecipeInfo recipeInfo, @Nullable ServerPlayer player) {
        for (var o : observers) {
            if (o.run(altar, recipeInfo, player)) return false;
        }
        return true;
    }

    public void register(Observer observer) {
        observers.add(observer);
    }

    @FunctionalInterface
    public interface Observer {

        boolean run(AltarBlockEntity altar, RecipeInfo recipeInfo, @Nullable ServerPlayer player);
    }
}

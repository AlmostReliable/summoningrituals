package com.almostreliable.summoningrituals.recipe.input;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.TriPredicate;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.function.Predicate;

public interface BaseEntityInput extends TriPredicate<ResourceLocation, Integer, Entity> {

    Table<ResourceLocation, Integer, Predicate<Entity>> DATA_VALIDATORS = HashBasedTable.create();

    @Override
    default boolean test(ResourceLocation recipeId, Integer inputIndex, Entity entity) {
        var predicate = DATA_VALIDATORS.get(recipeId, inputIndex);
        return predicate == null || predicate.test(entity);
    }
}

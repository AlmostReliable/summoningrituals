package com.almostreliable.summoningrituals.recipe.input;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.TriPredicate;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import java.util.function.Predicate;

public interface BaseEntityInput extends TriPredicate<Identifier, Integer, Entity> {

    Table<Identifier, Integer, Predicate<Entity>> DATA_VALIDATORS = HashBasedTable.create();
    Table<Identifier, Integer, Predicate<Entity>> FAKE_DATA_VALIDATORS = HashBasedTable.create();
    Table<Identifier, Integer, Predicate<Entity>> PENDING_DATA_VALIDATORS = Tables.synchronizedTable(HashBasedTable.create());
    Table<Identifier, Integer, Predicate<Entity>> PENDING_FAKE_DATA_VALIDATORS = Tables.synchronizedTable(HashBasedTable.create());

    default boolean test(Table<Identifier, Integer, Predicate<Entity>> validators, Identifier recipeId, Integer inputIndex, Entity entity) {
        var predicate = validators.get(recipeId, inputIndex);
        return predicate == null || predicate.test(entity);
    }
}

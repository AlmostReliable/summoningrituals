package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.type.TypeInfo;

public class LootItemConditionComponent implements RecipeComponent<LootItemCondition> {

    public static final LootItemConditionComponent INSTANCE = new LootItemConditionComponent();

    @Override
    public Codec<LootItemCondition> codec() {
        return LootItemCondition.DIRECT_CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(LootItemCondition.class);
    }

    @Override
    public String toString() {
        return SummoningRituals.getRL("loot_condition").toString();
    }
}

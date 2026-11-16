package com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import com.mojang.serialization.Codec;
import dev.uncandango.kubegroovy.recipe.component.RecipeComponent;
import dev.uncandango.kubegroovy.recipe.component.RecipeComponentType;
import dev.uncandango.kubegroovy.script.type.TypeInfo;

public record LootItemConditionComponent(ResourceKey<RecipeComponentType<?>> type) implements RecipeComponent<LootItemCondition> {

    public static final ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(SummoningRituals.getRL("loot_condition"));
    public static final LootItemConditionComponent INSTANCE = new LootItemConditionComponent(TYPE);

    @Override
    public Codec<LootItemCondition> codec() {
        return LootItemCondition.DIRECT_CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(LootItemCondition.class);
    }
}

package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.recipe.condition.pattern.BlockPatternCondition;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public record BlockPatternConditionComponent(RecipeComponentType<?> type) implements RecipeComponent<BlockPatternCondition> {

    public static final RecipeComponentType<BlockPatternCondition> TYPE = RecipeComponentType.unit(
        SummoningRituals.getRL("block_pattern_condition"),
        BlockPatternConditionComponent::new
    );

    @Override
    public Codec<BlockPatternCondition> codec() {
        return BlockPatternCondition.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(BlockPatternCondition.class);
    }
}

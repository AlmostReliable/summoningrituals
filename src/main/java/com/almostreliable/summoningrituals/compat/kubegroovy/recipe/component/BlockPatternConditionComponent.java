package com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.recipe.condition.pattern.BlockPatternCondition;

import net.minecraft.resources.ResourceKey;

import com.mojang.serialization.Codec;
import dev.uncandango.kubegroovy.recipe.component.RecipeComponent;
import dev.uncandango.kubegroovy.recipe.component.RecipeComponentType;
import dev.uncandango.kubegroovy.script.type.TypeInfo;

public record BlockPatternConditionComponent(ResourceKey<RecipeComponentType<?>> type) implements RecipeComponent<BlockPatternCondition> {

    public static final ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(
        SummoningRituals.getRL("block_pattern_condition")
    );
    public static final BlockPatternConditionComponent INSTANCE = new BlockPatternConditionComponent(TYPE);

    @Override
    public Codec<BlockPatternCondition> codec() {
        return BlockPatternCondition.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(BlockPatternCondition.class);
    }
}

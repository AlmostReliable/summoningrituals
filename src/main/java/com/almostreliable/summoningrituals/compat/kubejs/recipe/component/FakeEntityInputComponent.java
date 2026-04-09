package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.recipe.input.FakeEntityInput;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public record FakeEntityInputComponent(RecipeComponentType<?> type) implements RecipeComponent<FakeEntityInput> {

    public static final RecipeComponentType<FakeEntityInput> TYPE = RecipeComponentType.unit(
        SummoningRituals.getRL("fake_entity_input"),
        FakeEntityInputComponent::new
    );

    @Override
    public Codec<FakeEntityInput> codec() {
        return FakeEntityInput.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(FakeEntityInput.class);
    }
}

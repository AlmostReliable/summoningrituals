package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.recipe.input.FakeEntityInput;

import net.minecraft.resources.ResourceKey;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public record FakeEntityInputComponent(ResourceKey<RecipeComponentType<?>> type) implements RecipeComponent<FakeEntityInput> {

    public static final ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(SummoningRituals.getRL("fake_entity_input"));
    public static final FakeEntityInputComponent INSTANCE = new FakeEntityInputComponent(TYPE);
    private static final Codec<FakeEntityInput> CODEC = FakeEntityInput.CODEC.validate(input -> input.displayItem() == ItemOutputComponent.EMPTY
        ? DataResult.error(() -> "Fake entity input display item must not be empty")
        : DataResult.success(input));

    @Override
    public Codec<FakeEntityInput> codec() {
        return CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(FakeEntityInput.class);
    }
}

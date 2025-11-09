package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.recipe.output.CommandOutput;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.List;

public record CommandOutputComponent(RecipeComponentType<?> type) implements RecipeComponent<CommandOutput> {

    public static final RecipeComponentType<CommandOutput> TYPE = RecipeComponentType.unit(
        SummoningRituals.getRL("command_output"),
        CommandOutputComponent::new
    );

    @Override
    public Codec<CommandOutput> codec() {
        return CommandOutput.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(CommandOutput.class)
            .or(TypeInfo.of(List.class).withParams(TypeInfo.STRING))
            .or(TypeInfo.STRING_ARRAY)
            .or(TypeInfo.STRING);
    }
}

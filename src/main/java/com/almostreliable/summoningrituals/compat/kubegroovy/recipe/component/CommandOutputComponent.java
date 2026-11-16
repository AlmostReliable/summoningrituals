package com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.recipe.output.CommandOutput;

import net.minecraft.resources.ResourceKey;

import com.mojang.serialization.Codec;
import dev.uncandango.kubegroovy.recipe.component.RecipeComponent;
import dev.uncandango.kubegroovy.recipe.component.RecipeComponentType;
import dev.uncandango.kubegroovy.script.type.TypeInfo;

import java.util.List;

public record CommandOutputComponent(ResourceKey<RecipeComponentType<?>> type) implements RecipeComponent<CommandOutput> {

    public static final ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(SummoningRituals.getRL("command_output"));
    public static final CommandOutputComponent INSTANCE = new CommandOutputComponent(TYPE);

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

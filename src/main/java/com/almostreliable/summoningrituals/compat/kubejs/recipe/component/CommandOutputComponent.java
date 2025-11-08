package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.recipe.output.CommandOutput;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.wrap.TypeWrapperFactory;

import java.util.ArrayList;
import java.util.List;

public class CommandOutputComponent implements RecipeComponent<CommandOutput>, TypeWrapperFactory<CommandOutput> {

    public static final CommandOutputComponent INSTANCE = new CommandOutputComponent();

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

    @Override
    public CommandOutput wrap(Context cx, KubeRecipe recipe, Object from) {
        List<?> list = ListJS.of(from);
        if (list != null && !list.isEmpty()) {
            var result = new ArrayList<String>();
            for (var item : list) {
                if (item instanceof String s) {
                    result.add(normalizeCommand(s));
                }
            }
            return new CommandOutput(result);
        }

        if (from instanceof String s) {
            return new CommandOutput(normalizeCommand(s));
        }

        var exception = new KubeRuntimeException("invalid command output: " + from);
        if (recipe != null) {
            exception.source(recipe.sourceLine);
        }
        throw exception;
    }

    @Override
    public String toString() {
        return SummoningRituals.getRL("command_output").toString();
    }

    @Override
    public CommandOutput wrap(Context cx, Object from, TypeInfo target) {
        return wrap(cx, null, from);
    }

    public static String normalizeCommand(String command) {
        return command.startsWith("/") ? command.substring(1) : command;
    }
}

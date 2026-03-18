package com.almostreliable.summoningrituals.compat.kubejs.wrapper;

import com.almostreliable.summoningrituals.recipe.output.CommandOutput;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.wrap.TypeWrapperFactory;

import java.util.ArrayList;

public final class CommandOutputTypeWrapper implements TypeWrapperFactory<CommandOutput> {

    public static final CommandOutputTypeWrapper INSTANCE = new CommandOutputTypeWrapper();

    private CommandOutputTypeWrapper() {}

    @Override
    public CommandOutput wrap(Context cx, Object from, TypeInfo target) {
        if (from instanceof CommandOutput o) {
            return o;
        }

        var list = ListJS.of(from);
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

        throw new KubeRuntimeException("invalid command output: " + from).source(SourceLine.of(cx));
    }

    public static String normalizeCommand(String command) {
        return command.startsWith("/") ? command.substring(1) : command;
    }
}

package com.almostreliable.summoningrituals.compat.kubegroovy.wrapper;

import com.almostreliable.summoningrituals.recipe.output.CommandOutput;

import dev.uncandango.kubegroovy.error.KubeRuntimeException;
import dev.uncandango.kubegroovy.script.SourceLine;
import dev.uncandango.kubegroovy.util.ListGY;
import dev.uncandango.kubegroovy.script.bridge.Context;
import dev.uncandango.kubegroovy.script.type.TypeInfo;
import dev.uncandango.kubegroovy.script.bridge.TypeWrapperFactory;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

public final class CommandOutputTypeWrapper implements TypeWrapperFactory<CommandOutput> {

    public static final CommandOutputTypeWrapper INSTANCE = new CommandOutputTypeWrapper();

    private CommandOutputTypeWrapper() {}

    @Override
    public CommandOutput wrap(@Nullable Object from, TypeInfo target) {
        var cx = Context.getCurrent();
        if (from instanceof CommandOutput o) {
            return o;
        }

        var list = ListGY.of(from);
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

        throw new KubeRuntimeException("invalid command output: " + from).source(SourceLine.of());
    }

    public static String normalizeCommand(String command) {
        return command.startsWith("/") ? command.substring(1) : command;
    }
}

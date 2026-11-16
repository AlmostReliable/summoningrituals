package com.almostreliable.summoningrituals.compat.kubegroovy.wrapper;

import com.almostreliable.summoningrituals.recipe.container.EntityInfo;

import dev.uncandango.kubegroovy.error.InvalidRecipeComponentValueException;
import dev.uncandango.kubegroovy.error.KubeRuntimeException;
import dev.uncandango.kubegroovy.recipe.RecipeScriptContext;
import dev.uncandango.kubegroovy.recipe.component.RecipeComponent;
import dev.uncandango.kubegroovy.script.SourceLine;

import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public final class EntityComponentTypeWrapper {

    private EntityComponentTypeWrapper() {}

    public static <T> T wrap(
        RecipeComponent<?> component,
        RecipeScriptContext cx,
        @Nullable Object from,
        Class<T> type,
        Function<EntityInfo, T> factory
    ) {
        if (from == null) throw new KubeRuntimeException("invalid summoning entity: null").source(SourceLine.of());

        if (type.isInstance(from)) {
            return type.cast(from);
        }

        try {
            var entityInfo = EntityInfoTypeWrapper.INSTANCE.wrap(from, EntityInfoTypeWrapper.TYPE_INFO);
            return factory.apply(entityInfo);
        } catch (KubeRuntimeException e) {
            throw new InvalidRecipeComponentValueException(e.getMessage(), component, from).source(SourceLine.of());
        }
    }
}

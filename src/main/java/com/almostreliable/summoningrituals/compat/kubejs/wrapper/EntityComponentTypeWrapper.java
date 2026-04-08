package com.almostreliable.summoningrituals.compat.kubejs.wrapper;

import com.almostreliable.summoningrituals.recipe.container.EntityInfo;

import dev.latvian.mods.kubejs.error.InvalidRecipeComponentValueException;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.script.SourceLine;

import java.util.function.Function;

public final class EntityComponentTypeWrapper {

    private EntityComponentTypeWrapper() {}

    public static <T> T wrap(
        RecipeComponent<?> component, RecipeScriptContext cx, Object from, Class<T> type,
        Function<EntityInfo, T> factory
    ) {
        if (type.isInstance(from)) {
            return type.cast(from);
        }

        try {
            var entityInfo = EntityInfoTypeWrapper.INSTANCE.wrap(cx.cx(), from, EntityInfoTypeWrapper.TYPE_INFO);
            return factory.apply(entityInfo);
        } catch (KubeRuntimeException e) {
            throw new InvalidRecipeComponentValueException(e.getMessage(), component, from).source(SourceLine.of(cx.cx()));
        }
    }
}

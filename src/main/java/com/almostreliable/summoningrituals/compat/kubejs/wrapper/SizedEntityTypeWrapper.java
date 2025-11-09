package com.almostreliable.summoningrituals.compat.kubejs.wrapper;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import dev.latvian.mods.kubejs.error.InvalidRecipeComponentValueException;
import dev.latvian.mods.kubejs.holder.HolderWrapper;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.function.BiFunction;

public final class SizedEntityTypeWrapper {

    public static final TypeInfo ENTITY_TYPE_INFO = TypeInfo.of(EntityType.class);

    private SizedEntityTypeWrapper() {}

    public static <T> T wrap(
        RecipeComponent<?> component, RecipeScriptContext cx, Object from, Class<T> type,
        BiFunction<Holder<EntityType<?>>, Integer, T> factory
    ) {
        var o = from;

        if (type.isInstance(o)) {
            return type.cast(o);
        }

        String sequence = null;
        if (o instanceof ResourceLocation location) {
            sequence = location.toString();
        } else if (o instanceof String s) {
            sequence = s;
        }

        var count = 1;
        if (sequence != null) {
            sequence = sequence.trim();

            var spaceIndex = sequence.indexOf(' ');
            if (spaceIndex >= 2 && sequence.indexOf('x') == spaceIndex - 1) {
                count = Integer.parseInt(sequence.substring(0, spaceIndex - 1));
                o = sequence.substring(spaceIndex + 1);
            }
        }

        Holder<EntityType<?>> entityHolder;
        try {
            entityHolder = Cast.to(HolderWrapper.wrap((KubeJSContext) cx.cx(), o, ENTITY_TYPE_INFO));
        } catch (Exception e) {
            throw new InvalidRecipeComponentValueException("invalid summoning entity", component, from).source(cx.recipe().sourceLine);
        }

        if (entityHolder instanceof DeferredHolder<?, ?>) {
            throw new InvalidRecipeComponentValueException(
                "unknown entity id for summoning entity",
                component,
                from
            ).source(cx.recipe().sourceLine);
        }
        return factory.apply(entityHolder, count);
    }
}

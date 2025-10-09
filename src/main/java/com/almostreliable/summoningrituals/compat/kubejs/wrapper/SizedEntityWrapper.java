package com.almostreliable.summoningrituals.compat.kubejs.wrapper;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import dev.latvian.mods.kubejs.holder.HolderWrapper;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.function.BiFunction;

public interface SizedEntityWrapper {

    TypeInfo ENTITY_TYPE_INFO = TypeInfo.of(EntityType.class);

    static <T> T wrap(Context cx, Object from, Class<T> type, BiFunction<Holder<EntityType<?>>, Integer, T> factory) {
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

        try {
            //noinspection unchecked
            var entityHolder = (Holder<EntityType<?>>) HolderWrapper.wrap((KubeJSContext) cx, o, ENTITY_TYPE_INFO);
            return factory.apply(entityHolder, count);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid summoning entity: " + o, e);
        }
    }
}

package com.almostreliable.summoningrituals.compat.kubejs.wrapper;

import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityBuilder;
import com.almostreliable.summoningrituals.recipe.container.EntityInfo;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.holder.HolderWrapper;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.wrap.TypeWrapperFactory;

public class EntityInfoTypeWrapper implements TypeWrapperFactory<EntityInfo> {

    public static final EntityInfoTypeWrapper INSTANCE = new EntityInfoTypeWrapper();
    public static final TypeInfo TYPE_INFO = TypeInfo.of(EntityInfo.class);
    public static final TypeInfo ENTITY_TYPE_INFO = TypeInfo.of(EntityType.class);

    @Override
    public EntityInfo wrap(Context cx, Object from, TypeInfo target) {
        var o = from;
        if (o instanceof EntityInfo entityInfo) {
            return entityInfo;
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
            entityHolder = Cast.to(HolderWrapper.wrap((KubeJSContext) cx, o, ENTITY_TYPE_INFO));
        } catch (Exception e) {
            throw new KubeRuntimeException("invalid summoning entity: " + o).source(SourceLine.of(cx));
        }

        if (entityHolder instanceof DeferredHolder<?, ?>) {
            throw new KubeRuntimeException("unknown entity id for summoning entity: " + o).source(SourceLine.of(cx));
        }

        return new SummoningEntityBuilder(entityHolder, count).build();
    }
}

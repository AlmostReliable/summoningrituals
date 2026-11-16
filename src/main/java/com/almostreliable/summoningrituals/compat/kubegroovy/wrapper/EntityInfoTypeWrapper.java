package com.almostreliable.summoningrituals.compat.kubegroovy.wrapper;

import com.almostreliable.summoningrituals.compat.kubegroovy.builder.SummoningEntityBuilder;
import com.almostreliable.summoningrituals.recipe.container.EntityInfo;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import dev.uncandango.kubegroovy.error.KubeRuntimeException;
import dev.uncandango.kubegroovy.holder.HolderWrapper;
import dev.uncandango.kubegroovy.script.KubeGroovyContext;
import dev.uncandango.kubegroovy.script.SourceLine;
import dev.uncandango.kubegroovy.util.Cast;
import dev.uncandango.kubegroovy.script.bridge.Context;
import dev.uncandango.kubegroovy.script.type.TypeInfo;
import dev.uncandango.kubegroovy.script.bridge.TypeWrapperFactory;

import org.jspecify.annotations.Nullable;

public class EntityInfoTypeWrapper implements TypeWrapperFactory<EntityInfo> {

    public static final EntityInfoTypeWrapper INSTANCE = new EntityInfoTypeWrapper();
    public static final TypeInfo TYPE_INFO = TypeInfo.of(EntityInfo.class);
    public static final TypeInfo ENTITY_TYPE_INFO = TypeInfo.of(EntityType.class);

    @Override
    public EntityInfo wrap(@Nullable Object from, TypeInfo target) {
        var o = from;
        if (o instanceof EntityInfo entityInfo) {
            return entityInfo;
        }

        String sequence = null;
        if (o instanceof Identifier location) {
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

        if (!(Context.getCurrent() instanceof KubeGroovyContext cx)) {
            throw new KubeRuntimeException("invalid summoning entity: " + o).source(SourceLine.of());
        }

        Holder<EntityType<?>> entityHolder;
        try {
            entityHolder = Cast.to(HolderWrapper.wrap(cx, o, ENTITY_TYPE_INFO));
        } catch (Exception e) {
            throw new KubeRuntimeException("invalid summoning entity: " + o).source(SourceLine.of());
        }

        if (entityHolder instanceof DeferredHolder<?, ?>) {
            throw new KubeRuntimeException("unknown entity id for summoning entity: " + o).source(SourceLine.of());
        }

        return new SummoningEntityBuilder(entityHolder, count).build();
    }
}

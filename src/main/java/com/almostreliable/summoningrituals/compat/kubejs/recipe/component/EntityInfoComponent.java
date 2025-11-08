package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningEntityBinding;
import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityBuilder;
import com.almostreliable.summoningrituals.recipe.EntityInfo;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.holder.HolderWrapper;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.function.BiFunction;

public class EntityInfoComponent implements RecipeComponent<EntityInfo> {

    public static final EntityInfoComponent INSTANCE = new EntityInfoComponent();
    public static final TypeInfo ENTITY_TYPE_INFO = TypeInfo.of(EntityType.class);

    @Override
    public Codec<EntityInfo> codec() {
        return EntityInfo.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(EntityInfo.class)
            .or(TypeInfo.of(SummoningEntityBuilder.class))
            .or(TypeInfo.of(Holder.class).withParams(ENTITY_TYPE_INFO))
            .or(TypeInfo.of(ResourceLocation.class))
            .or(TypeInfo.STRING);
    }

    @Override
    public EntityInfo wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from instanceof SummoningEntityBuilder builder) {
            return builder.build();
        }

        return wrapSizedEntity(
            cx,
            from,
            recipe.sourceLine,
            EntityInfo.class,
            (entity, count) -> SummoningEntityBinding.input(entity, count).build()
        );
    }

    @Override
    public String toString() {
        return SummoningRituals.getRL("entity_info").toString();
    }

    public static <T> T wrapSizedEntity(
        Context cx, Object from, SourceLine source, Class<T> type, BiFunction<Holder<EntityType<?>>, Integer, T> factory
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

        try {
            //noinspection unchecked
            var entityHolder = (Holder<EntityType<?>>) HolderWrapper.wrap((KubeJSContext) cx, o, ENTITY_TYPE_INFO);
            return factory.apply(entityHolder, count);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid summoning entity: " + o, e);
        }
    }
}

package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningOutputBinding;
import com.almostreliable.summoningrituals.compat.kubejs.builder.EntityOutputBuilder;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.holder.HolderWrapper;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;

public class EntityOutputComponent implements RecipeComponent<EntityOutput> {

    public static final EntityOutputComponent INSTANCE = new EntityOutputComponent();
    private static final TypeInfo ENTITY_TYPE_INFO = TypeInfo.of(EntityType.class);

    @Override
    public Codec<EntityOutput> codec() {
        return EntityOutput.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(EntityOutput.class)
            .or(TypeInfo.of(EntityOutputBuilder.class))
            .or(TypeInfo.of(Holder.class).withParams(ENTITY_TYPE_INFO))
            .or(TypeInfo.of(ResourceLocation.class))
            .or(TypeInfo.STRING);
    }

    @Override
    public EntityOutput wrap(Context cx, KubeRecipe recipe, Object from) {
        var o = from;

        if (o instanceof EntityOutput entityOutput) {
            return entityOutput;
        }

        if (o instanceof EntityOutputBuilder builder) {
            return builder.build();
        }

        String sequence = null;
        if (o instanceof ResourceLocation location) {
            sequence = location.toString();
        } else if (o instanceof String s) {
            sequence = s;
        }

        var count = 0;
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
            return SummoningOutputBinding.entityOutput(entityHolder, count).build();
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid summoning entity output: " + o, e);
        }
    }

    @Override
    public String toString() {
        return SummoningRituals.getRL("entity_output").toString();
    }
}

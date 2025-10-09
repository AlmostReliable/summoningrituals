package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningOutputBinding;
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
    private static final TypeInfo HOLDER_TYPE_INFO = TypeInfo.of(Holder.class).withParams(TypeInfo.of(EntityType.class));

    @Override
    public Codec<EntityOutput> codec() {
        return EntityOutput.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(EntityOutput.class)
            .or(TypeInfo.of(EntityOutput.Builder.class))
            .or(HOLDER_TYPE_INFO)
            .or(TypeInfo.of(ResourceLocation.class))
            .or(TypeInfo.STRING);
    }

    @Override
    public EntityOutput wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from instanceof EntityOutput o) {
            return o;
        }

        if (from instanceof EntityOutput.Builder builder) {
            return builder.build();
        }

        String sequence = null;
        if (from instanceof ResourceLocation location) {
            sequence = location.toString();
        } else if (from instanceof String string) {
            sequence = string;
        }
        if (sequence != null) {
            sequence = sequence.trim();

            var count = 1;
            var spaceIndex = sequence.indexOf(' ');

            if (spaceIndex >= 2 && sequence.indexOf('x') == spaceIndex - 1) {
                count = Integer.parseInt(sequence.substring(0, spaceIndex - 1));
                sequence = sequence.substring(spaceIndex + 1);
            }

            try {
                //noinspection unchecked
                var entityHolder = (Holder<EntityType<?>>) HolderWrapper.wrap((KubeJSContext) cx, sequence, HOLDER_TYPE_INFO);
                return SummoningOutputBinding.entityOutput(entityHolder, count).build();
            } catch (Exception ignored) {
                // ignored
            }
        }

        try {
            //noinspection unchecked
            var entityHolder = (Holder<EntityType<?>>) HolderWrapper.wrap((KubeJSContext) cx, from, HOLDER_TYPE_INFO);
            return SummoningOutputBinding.entityOutput(entityHolder).build();
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid summoning entity output: " + from, e);
        }
    }

    @Override
    public String toString() {
        return SummoningRituals.getRL("entity_output").toString();
    }
}

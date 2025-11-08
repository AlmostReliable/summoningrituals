package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningEntityBinding;
import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityOutputBuilder;
import com.almostreliable.summoningrituals.recipe.EntityInfo;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.Optional;

public class EntityOutputComponent implements RecipeComponent<EntityOutput> {

    public static final EntityOutputComponent INSTANCE = new EntityOutputComponent();

    @Override
    public Codec<EntityOutput> codec() {
        return EntityOutput.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(EntityOutput.class)
            .or(TypeInfo.of(EntityInfo.class))
            .or(TypeInfo.of(SummoningEntityOutputBuilder.class))
            .or(TypeInfo.of(Holder.class).withParams(EntityInfoComponent.ENTITY_TYPE_INFO))
            .or(TypeInfo.of(ResourceLocation.class))
            .or(TypeInfo.STRING);
    }

    @Override
    public EntityOutput wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from instanceof SummoningEntityOutputBuilder builder) {
            return builder.buildOutput();
        }

        if (from instanceof EntityInfo info) {
            return new EntityOutput(info, Optional.empty(), Optional.empty());
        }

        return EntityInfoComponent.wrapSizedEntity(
            cx,
            from,
            recipe.sourceLine,
            EntityOutput.class,
            (entity, count) -> SummoningEntityBinding.output(entity, count).buildOutput()
        );
    }

    @Override
    public String toString() {
        return SummoningRituals.getRL("entity_output").toString();
    }
}

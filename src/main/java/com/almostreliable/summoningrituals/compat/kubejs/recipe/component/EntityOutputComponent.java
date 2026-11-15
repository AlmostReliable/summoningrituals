package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningEntityBinding;
import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityOutputBuilder;
import com.almostreliable.summoningrituals.compat.kubejs.wrapper.EntityComponentTypeWrapper;
import com.almostreliable.summoningrituals.compat.kubejs.wrapper.EntityInfoTypeWrapper;
import com.almostreliable.summoningrituals.recipe.container.EntityInfo;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.Optional;

public record EntityOutputComponent(ResourceKey<RecipeComponentType<?>> type) implements RecipeComponent<EntityOutput> {

    public static final ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(SummoningRituals.getRL("entity_output"));
    public static final EntityOutputComponent INSTANCE = new EntityOutputComponent(TYPE);

    @Override
    public Codec<EntityOutput> codec() {
        return EntityOutput.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(EntityOutput.class)
            .or(TypeInfo.of(EntityInfo.class))
            .or(TypeInfo.of(SummoningEntityOutputBuilder.class))
            .or(TypeInfo.of(Holder.class).withParams(EntityInfoTypeWrapper.ENTITY_TYPE_INFO))
            .or(TypeInfo.of(Identifier.class))
            .or(TypeInfo.STRING);
    }

    @Override
    public EntityOutput wrap(RecipeScriptContext cx, Object from) {
        if (from instanceof SummoningEntityOutputBuilder builder) {
            return builder.buildOutput();
        }

        if (from instanceof EntityInfo info) {
            return new EntityOutput(info, Optional.empty(), Optional.empty());
        }

        return EntityComponentTypeWrapper.wrap(
            this,
            cx,
            from,
            EntityOutput.class,
            entity -> SummoningEntityBinding.output(entity).buildOutput()
        );
    }
}

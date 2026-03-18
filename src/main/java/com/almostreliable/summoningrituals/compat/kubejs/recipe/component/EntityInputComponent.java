package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningEntityBinding;
import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityInputBuilder;
import com.almostreliable.summoningrituals.compat.kubejs.wrapper.EntityComponentTypeWrapper;
import com.almostreliable.summoningrituals.compat.kubejs.wrapper.EntityInfoTypeWrapper;
import com.almostreliable.summoningrituals.recipe.EntityInfo;
import com.almostreliable.summoningrituals.recipe.EntityInput;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public record EntityInputComponent(RecipeComponentType<?> type) implements RecipeComponent<EntityInput> {

    public static final RecipeComponentType<EntityInput> TYPE = RecipeComponentType.unit(
        SummoningRituals.getRL("entity_input"),
        EntityInputComponent::new
    );

    @Override
    public Codec<EntityInput> codec() {
        return EntityInput.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(EntityInput.class)
            .or(TypeInfo.of(EntityInfo.class))
            .or(TypeInfo.of(SummoningEntityInputBuilder.class))
            .or(TypeInfo.of(Holder.class).withParams(EntityInfoTypeWrapper.ENTITY_TYPE_INFO))
            .or(TypeInfo.of(ResourceLocation.class))
            .or(TypeInfo.STRING);
    }

    @Override
    public EntityInput wrap(RecipeScriptContext cx, Object from) {
        if (from instanceof SummoningEntityInputBuilder builder) {
            return builder.buildInput();
        }

        if (from instanceof EntityInfo info) {
            return new EntityInput(info);
        }

        return EntityComponentTypeWrapper.wrap(
            this,
            cx,
            from,
            EntityInput.class,
            entity -> SummoningEntityBinding.input(entity).buildInput()
        );
    }
}

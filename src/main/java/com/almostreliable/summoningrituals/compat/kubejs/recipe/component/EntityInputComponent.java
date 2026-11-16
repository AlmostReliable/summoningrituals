package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningEntityBinding;
import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityInputBuilder;
import com.almostreliable.summoningrituals.compat.kubejs.wrapper.EntityComponentTypeWrapper;
import com.almostreliable.summoningrituals.compat.kubejs.wrapper.EntityInfoTypeWrapper;
import com.almostreliable.summoningrituals.recipe.container.EntityInfo;
import com.almostreliable.summoningrituals.recipe.input.EntityInput;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

import org.jspecify.annotations.Nullable;

public record EntityInputComponent(ResourceKey<RecipeComponentType<?>> type) implements RecipeComponent<EntityInput> {

    public static final ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(SummoningRituals.getRL("entity_input"));
    public static final EntityInputComponent INSTANCE = new EntityInputComponent(TYPE);

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
            .or(TypeInfo.of(Identifier.class))
            .or(TypeInfo.STRING);
    }

    @Override
    public EntityInput wrap(RecipeScriptContext cx, @Nullable Object from) {
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

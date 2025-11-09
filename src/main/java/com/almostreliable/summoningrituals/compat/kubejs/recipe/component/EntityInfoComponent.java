package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningEntityBinding;
import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityBuilder;
import com.almostreliable.summoningrituals.compat.kubejs.wrapper.SizedEntityTypeWrapper;
import com.almostreliable.summoningrituals.recipe.EntityInfo;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public record EntityInfoComponent(RecipeComponentType<?> type) implements RecipeComponent<EntityInfo> {

    public static final RecipeComponentType<EntityInfo> TYPE = RecipeComponentType.unit(
        SummoningRituals.getRL("entity_info"),
        EntityInfoComponent::new
    );

    @Override
    public Codec<EntityInfo> codec() {
        return EntityInfo.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(EntityInfo.class)
            .or(TypeInfo.of(SummoningEntityBuilder.class))
            .or(TypeInfo.of(Holder.class).withParams(SizedEntityTypeWrapper.ENTITY_TYPE_INFO))
            .or(TypeInfo.of(ResourceLocation.class))
            .or(TypeInfo.STRING);
    }

    @Override
    public EntityInfo wrap(RecipeScriptContext cx, Object from) {
        if (from instanceof SummoningEntityBuilder builder) {
            return builder.build();
        }

        return SizedEntityTypeWrapper.wrap(
            this,
            cx,
            from,
            EntityInfo.class,
            (entity, count) -> SummoningEntityBinding.input(entity, count).build()
        );
    }
}

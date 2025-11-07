package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningEntityBinding;
import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityBuilder;
import com.almostreliable.summoningrituals.compat.kubejs.wrapper.SizedEntityWrapper;
import com.almostreliable.summoningrituals.recipe.EntityInfo;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;

public class EntityInfoComponent implements RecipeComponent<EntityInfo> {

    public static final EntityInfoComponent INSTANCE = new EntityInfoComponent();

    @Override
    public Codec<EntityInfo> codec() {
        return EntityInfo.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(EntityInfo.class)
            .or(TypeInfo.of(SummoningEntityBuilder.class))
            .or(TypeInfo.of(Holder.class).withParams(SizedEntityWrapper.ENTITY_TYPE_INFO))
            .or(TypeInfo.of(ResourceLocation.class))
            .or(TypeInfo.STRING);
    }

    @Override
    public EntityInfo wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from instanceof SummoningEntityBuilder builder) {
            return builder.build();
        }

        return SizedEntityWrapper.wrap(cx, from, EntityInfo.class, (entity, count) -> SummoningEntityBinding.input(entity, count).build());
    }

    @Override
    public String toString() {
        return SummoningRituals.getRL("entity_info").toString();
    }
}

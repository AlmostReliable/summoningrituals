package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.wrapper.SizedEntityWrapper;
import com.almostreliable.summoningrituals.recipe.input.EntityInput;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;

public class EntityInputComponent implements RecipeComponent<EntityInput> {

    public static final EntityInputComponent INSTANCE = new EntityInputComponent();

    @Override
    public Codec<EntityInput> codec() {
        return EntityInput.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(EntityInput.class)
            .or(TypeInfo.of(Holder.class).withParams(SizedEntityWrapper.ENTITY_TYPE_INFO))
            .or(TypeInfo.of(ResourceLocation.class))
            .or(TypeInfo.STRING);
    }

    @Override
    public EntityInput wrap(Context cx, KubeRecipe recipe, Object from) {
        return SizedEntityWrapper.wrap(cx, from, EntityInput.class, EntityInput::new);
    }

    @Override
    public String toString() {
        return SummoningRituals.getRL("entity_input").toString();
    }
}

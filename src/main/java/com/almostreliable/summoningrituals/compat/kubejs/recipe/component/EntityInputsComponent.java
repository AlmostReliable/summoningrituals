package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.recipe.input.EntityInputs;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.type.TypeInfo;

public class EntityInputsComponent implements RecipeComponent<EntityInputs> {

    public static final EntityInputsComponent INSTANCE = new EntityInputsComponent();

    @Override
    public Codec<EntityInputs> codec() {
        return EntityInputs.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(EntityInputs.class);
    }

    @Override
    public String toString() {
        return SummoningRituals.getRL("entity_inputs").toString();
    }
}

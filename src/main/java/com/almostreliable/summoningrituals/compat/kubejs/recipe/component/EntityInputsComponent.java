package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningInputBinding;
import com.almostreliable.summoningrituals.recipe.input.EntityInputs;

import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.EntityType;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.holder.HolderWrapper;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;

public class EntityInputsComponent implements RecipeComponent<EntityInputs> {

    public static final EntityInputsComponent INSTANCE = new EntityInputsComponent();
    private static final TypeInfo HOLDER_TYPE_INFO = TypeInfo.of(HolderSet.class).withParams(TypeInfo.of(EntityType.class));

    @Override
    public Codec<EntityInputs> codec() {
        return EntityInputs.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(EntityInputs.class)
            .or(TypeInfo.of(EntityInputs.Builder.class))
            .or(HOLDER_TYPE_INFO);
    }

    @Override
    public EntityInputs wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from instanceof EntityInputs o) {
            return o;
        }

        if (from instanceof EntityInputs.Builder builder) {
            return builder.build();
        }

        try {
            //noinspection unchecked
            var entityHolders = (HolderSet<EntityType<?>>) HolderWrapper.wrapSet((KubeJSContext) cx, from, HOLDER_TYPE_INFO);
            return SummoningInputBinding.entityInputs().add(entityHolders).build();
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid summoning entity inputs: " + from, e);
        }
    }

    @Override
    public String toString() {
        return SummoningRituals.getRL("entity_inputs").toString();
    }
}

package com.almostreliable.summoningrituals.compat.viewer.emi.entity;

import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityBuilder;
import com.almostreliable.summoningrituals.compat.viewer.jei.entity.EntityIngredient;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import com.google.common.primitives.Ints;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;

public class EntityEmiStackSerializer implements EmiStackSerializer<EntityEmiStack> {

    @Override
    public EmiStack create(ResourceLocation id, DataComponentPatch componentChanges, long amount) {
        var entityType = BuiltInRegistries.ENTITY_TYPE.getHolder(id).orElseThrow();
        var entityInfo = new SummoningEntityBuilder(entityType, Ints.saturatedCast(amount)).build();
        return EntityEmiStack.input(new EntityIngredient(entityInfo));
    }

    @Override
    public String getType() {
        return ModConstants.MOD_ID + "_entity";
    }
}

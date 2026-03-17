package com.almostreliable.summoningrituals.compat.kubejs.builder;

import com.almostreliable.summoningrituals.recipe.EntityInput;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import dev.latvian.mods.rhino.util.HideFromJS;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class SummoningEntityInputBuilder extends SummoningEntityBuilder {

    @Nullable
    private Predicate<Entity> validator;

    public SummoningEntityInputBuilder(Holder<EntityType<?>> entity) {
        super(entity);
    }

    public SummoningEntityInputBuilder(Holder<EntityType<?>> entity, int count) {
        super(entity, count);
    }

    public SummoningEntityBuilder validator(Predicate<Entity> validator) {
        this.validator = validator;
        return this;
    }

    @HideFromJS
    public EntityInput buildInput() {
        var entityInfo = build();
        return new EntityInput(entityInfo, validator);
    }
}

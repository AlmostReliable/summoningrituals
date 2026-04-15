package com.almostreliable.summoningrituals.compat.kubejs.builder;

import com.almostreliable.summoningrituals.recipe.container.EntityInfo;
import com.almostreliable.summoningrituals.recipe.input.EntityInput;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;

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

    public SummoningEntityInputBuilder(EntityInfo entity) {
        super(entity);
    }

    @ReturnsSelf
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

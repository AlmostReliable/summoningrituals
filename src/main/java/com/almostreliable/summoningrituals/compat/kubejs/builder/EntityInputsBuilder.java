package com.almostreliable.summoningrituals.compat.kubejs.builder;

import com.almostreliable.summoningrituals.recipe.input.EntityInput;
import com.almostreliable.summoningrituals.recipe.input.EntityInputs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.EntityType;

import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.ArrayList;
import java.util.List;

public class EntityInputsBuilder {

    private final List<EntityInput> inputs = new ArrayList<>();
    private final BlockPos zone;

    public EntityInputsBuilder(BlockPos zone) {
        this.zone = zone;
    }

    public EntityInputsBuilder() {
        this(EntityInputs.DEFAULT_ZONE);
    }

    public EntityInputsBuilder add(Holder<EntityType<?>> entityType, int count) {
        inputs.add(new EntityInput(entityType, count));
        return this;
    }

    public EntityInputsBuilder add(Holder<EntityType<?>> entityType) {
        add(entityType, 1);
        return this;
    }

    public EntityInputsBuilder addAll(HolderSet<EntityType<?>> entityTypes) {
        entityTypes.forEach(this::add);
        return this;
    }

    @HideFromJS
    public EntityInputs build() {
        return new EntityInputs(inputs, zone);
    }
}

package com.almostreliable.summoningrituals.compat.kubejs.builder;

import com.almostreliable.summoningrituals.recipe.container.EntityInfo;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;

import dev.latvian.mods.rhino.util.HideFromJS;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SummoningEntityOutputBuilder extends SummoningEntityBuilder {

    @Nullable
    private BlockPos offset;
    @Nullable
    private BlockPos spread;

    public SummoningEntityOutputBuilder(Holder<EntityType<?>> entity) {
        super(entity);
    }

    public SummoningEntityOutputBuilder(Holder<EntityType<?>> entity, int count) {
        super(entity, count);
    }

    public SummoningEntityOutputBuilder(EntityInfo entity) {
        super(entity);
    }

    public SummoningEntityBuilder offset(BlockPos offset) {
        this.offset = offset;
        return this;
    }

    public SummoningEntityBuilder spread(BlockPos spread) {
        this.spread = spread;
        return this;
    }

    @HideFromJS
    public EntityOutput buildOutput() {
        var entityInfo = build();
        return new EntityOutput(entityInfo, Optional.ofNullable(offset), Optional.ofNullable(spread));
    }
}

package com.almostreliable.summoningrituals.compat.kubegroovy.builder;

import com.almostreliable.summoningrituals.recipe.container.EntityInfo;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;

import dev.uncandango.kubegroovy.script.annotations.HideFromGY;
import dev.uncandango.kubegroovy.script.annotations.ReturnsSelf;

import org.jspecify.annotations.Nullable;

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

    @ReturnsSelf
    public SummoningEntityBuilder offset(BlockPos offset) {
        this.offset = offset;
        return this;
    }

    @ReturnsSelf
    public SummoningEntityBuilder spread(BlockPos spread) {
        this.spread = spread;
        return this;
    }

    @HideFromGY
    public EntityOutput buildOutput() {
        var entityInfo = build();
        return new EntityOutput(entityInfo, Optional.ofNullable(offset), Optional.ofNullable(spread));
    }
}

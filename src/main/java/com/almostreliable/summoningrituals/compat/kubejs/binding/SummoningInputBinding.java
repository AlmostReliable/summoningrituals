package com.almostreliable.summoningrituals.compat.kubejs.binding;

import com.almostreliable.summoningrituals.recipe.input.EntityInputs;

import net.minecraft.core.BlockPos;

public interface SummoningInputBinding {

    static EntityInputs.Builder entityInputs(BlockPos zone) {
        return new EntityInputs.Builder(zone);
    }

    static EntityInputs.Builder entityInputs() {
        return new EntityInputs.Builder();
    }
}

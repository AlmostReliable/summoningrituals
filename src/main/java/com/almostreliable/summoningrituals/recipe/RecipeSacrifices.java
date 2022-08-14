package com.almostreliable.summoningrituals.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public class RecipeSacrifices {

    private final AABB bounds;
    private final NonNullList<Sacrifice> sacrifices;

    public RecipeSacrifices() {
        bounds = new AABB(0, 0, 0, 0, 0, 0);
        sacrifices = NonNullList.create();
    }

    public void addSacrifice(ResourceLocation id, int count) {
        sacrifices.add(new Sacrifice(id, count));
    }

    private record Sacrifice(ResourceLocation entity, int count) {}
}

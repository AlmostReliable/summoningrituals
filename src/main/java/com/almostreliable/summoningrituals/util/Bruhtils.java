package com.almostreliable.summoningrituals.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class Bruhtils {

    private Bruhtils() {}

    public static ResourceLocation getId(ForgeRegistryEntry<?> entry) {
        if (entry.getRegistryName() == null) {
            throw new IllegalArgumentException("entry has no registry name");
        }
        return entry.getRegistryName();
    }
}

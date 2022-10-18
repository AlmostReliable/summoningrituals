package com.almostreliable.summoningrituals.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public final class Bruhtils {

    private Bruhtils() {}

    public static ResourceLocation getId(Item item) {
        var id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) {
            throw new IllegalArgumentException("item has no registry name");
        }
        return id;
    }

    public static ResourceLocation getId(Block block) {
        var id = ForgeRegistries.BLOCKS.getKey(block);
        if (id == null) {
            throw new IllegalArgumentException("block has no registry name");
        }
        return id;
    }

    public static ResourceLocation getId(EntityType<?> entityType) {
        var id = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        if (id == null) {
            throw new IllegalArgumentException("entityType has no registry name");
        }
        return id;
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }
}

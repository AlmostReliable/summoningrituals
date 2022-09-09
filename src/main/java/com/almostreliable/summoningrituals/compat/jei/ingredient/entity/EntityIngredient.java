package com.almostreliable.summoningrituals.compat.jei.ingredient.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;

import javax.annotation.Nullable;

public class EntityIngredient {

    private final EntityType<?> entityType;
    private final int count;
    @Nullable private Entity entity;

    public EntityIngredient(EntityType<?> entityType, int count, CompoundTag tag) {
        this.entityType = entityType;
        this.count = count;
        var level = Minecraft.getInstance().level;
        if (level != null) {
            entity = entityType.create(level);
            if (entity != null && !tag.isEmpty()) {
                entity.load(tag);
            }
        }
    }

    public EntityIngredient(EntityType<?> entityType, int count) {
        this(entityType, count, new CompoundTag());
    }

    Component getDisplayName() {
        if (entity == null) return new TextComponent("Unknown Entity");
        return entity.getDisplayName();
    }

    EntityType<?> getEntityType() {
        return entityType;
    }

    public int getCount() {
        return count;
    }

    @Nullable
    public Entity getEntity() {
        return entity;
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public SpawnEggItem getEgg() {
        return SpawnEggItem.byId(entityType);
    }
}

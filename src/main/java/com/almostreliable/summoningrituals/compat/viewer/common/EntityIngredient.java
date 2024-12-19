package com.almostreliable.summoningrituals.compat.viewer.common;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;

import javax.annotation.Nullable;

public class EntityIngredient {

    private final Holder<EntityType<?>> entityType;
    private final int count;
    private final CompoundTag tag;
    @Nullable private Entity entity;

    public EntityIngredient(Holder<EntityType<?>> entityType, int count, CompoundTag tag) {
        this.entityType = entityType;
        this.count = count;
        this.tag = tag;
        var level = Minecraft.getInstance().level;
        if (level != null) {
            this.entity = entityType.value().create(level);
            if (this.entity != null && !tag.isEmpty()) {
                this.entity.load(tag);
            }
        }
    }

    public EntityIngredient(Holder<EntityType<?>> entityType, int count) {
        this(entityType, count, new CompoundTag());
    }

    public Component getDisplayName() {
        if (entity == null) return Component.literal("Unknown Entity");
        return entity.getDisplayName();
    }

    public MutableComponent getRegistryName() {
        var key = entityType.getKey();
        assert key != null;
        return Component.literal(key.location().toString());
    }

    public Holder<EntityType<?>> getEntityType() {
        return entityType;
    }

    public int getCount() {
        return count;
    }

    public CompoundTag getTag() {
        return tag;
    }

    @Nullable
    public Entity getEntity() {
        return entity;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public SpawnEggItem getEgg() {
        return SpawnEggItem.byId(entityType.value());
    }
}

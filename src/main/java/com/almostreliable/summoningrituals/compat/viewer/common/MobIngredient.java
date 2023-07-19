package com.almostreliable.summoningrituals.compat.viewer.common;

import com.almostreliable.summoningrituals.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;

import javax.annotation.Nullable;

public class MobIngredient {

    private final EntityType<?> mob;
    private final int count;
    private final CompoundTag tag;
    @Nullable private Entity entity;

    public MobIngredient(EntityType<?> mob, int count, CompoundTag tag) {
        this.mob = mob;
        this.count = count;
        this.tag = tag;
        var level = Minecraft.getInstance().level;
        if (level != null) {
            entity = mob.create(level);
            if (entity != null && !tag.isEmpty()) {
                entity.load(tag);
            }
        }
    }

    public MobIngredient(EntityType<?> mob, int count) {
        this(mob, count, new CompoundTag());
    }

    public Component getDisplayName() {
        if (entity == null) return Component.literal("Unknown Entity");
        return entity.getDisplayName();
    }

    public MutableComponent getRegistryName() {
        return Component.literal(Platform.getId(mob).toString());
    }

    public EntityType<?> getEntityType() {
        return mob;
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

    @Nullable
    public SpawnEggItem getEgg() {
        return SpawnEggItem.byId(mob);
    }
}

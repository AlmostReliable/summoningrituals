package com.almostreliable.summoningrituals.compat.viewer.jei.entity;

import com.almostreliable.summoningrituals.recipe.EntityInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import com.mojang.serialization.Codec;

import org.jetbrains.annotations.Nullable;

public final class EntityIngredient {

    // TODO: improve codec to only encode entity type holder
    public static final Codec<EntityIngredient> CODEC = EntityInfo.CODEC.xmap(EntityIngredient::new, EntityIngredient::getEntityInfo);

    private final EntityInfo entityInfo;
    @Nullable
    private Entity entity;

    public EntityIngredient(EntityInfo entityInfo) {
        this.entityInfo = entityInfo;
        var level = Minecraft.getInstance().level;
        if (level != null) {
            entity = entityInfo.entity().value().create(level);
            if (entity != null && entityInfo.data().isPresent()) {
                entity.load(entityInfo.data().get());
            }
        }
    }

    public Component getDisplayName() {
        if (entity == null) return Component.literal("Unknown Entity");
        return entity.getDisplayName();
    }

    public ResourceLocation getResourceLocation() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityInfo.entity().value());
    }

    public EntityInfo getEntityInfo() {
        return entityInfo;
    }

    @Nullable
    public Entity getEntity() {
        return entity;
    }

    @Nullable
    public ItemStack getEgg() {
        var item = SpawnEggItem.byId(entityInfo.entity().value());
        return item == null ? null : new ItemStack(item);
    }

    public EntityIngredient copy() {
        return new EntityIngredient(entityInfo);
    }
}

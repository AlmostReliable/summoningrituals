package com.almostreliable.summoningrituals.compat.viewer.jei.entity;

import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityBuilder;
import com.almostreliable.summoningrituals.recipe.EntityInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

public final class EntityIngredient {

    public static final Codec<EntityIngredient> CODEC = RecordCodecBuilder.create(i -> i.group(
        BuiltInRegistries.ENTITY_TYPE.holderByNameCodec().fieldOf(Entity.ID_TAG).forGetter(e -> e.entityInfo.entity())
    ).apply(i, e -> new EntityIngredient(new SummoningEntityBuilder(e).build())));

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

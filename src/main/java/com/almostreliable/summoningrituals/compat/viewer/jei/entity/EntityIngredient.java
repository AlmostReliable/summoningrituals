package com.almostreliable.summoningrituals.compat.viewer.jei.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

public class EntityIngredient {

    public static final Codec<EntityIngredient> CODEC = RecordCodecBuilder.create(i -> i.group(
        BuiltInRegistries.ENTITY_TYPE.holderByNameCodec().fieldOf("entity_type").forGetter(EntityIngredient::getEntityTypeHolder),
        Codec.INT.fieldOf("count").forGetter(EntityIngredient::getCount),
        CompoundTag.CODEC.fieldOf("data").forGetter(EntityIngredient::getData)
    ).apply(i, EntityIngredient::new));

    private final Holder<EntityType<?>> entityTypeHolder;
    private final int count;
    private final CompoundTag data;
    @Nullable
    private Entity entity;

    public EntityIngredient(Holder<EntityType<?>> entityTypeHolder, int count, CompoundTag data) {
        this.entityTypeHolder = entityTypeHolder;
        this.count = count;
        this.data = data;
        var level = Minecraft.getInstance().level;
        if (level != null) {
            entity = entityTypeHolder.value().create(level);
            if (entity != null && !data.isEmpty()) {
                entity.load(data);
            }
        }
    }

    public EntityIngredient(Holder<EntityType<?>> entityTypeHolder, int count) {
        this(entityTypeHolder, count, new CompoundTag());
    }

    public Component getDisplayName() {
        if (entity == null) return Component.literal("Unknown Entity");
        return entity.getDisplayName();
    }

    public ResourceLocation getResourceLocation() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getEntityType());
    }

    public Holder<EntityType<?>> getEntityTypeHolder() {
        return entityTypeHolder;
    }

    public EntityType<?> getEntityType() {
        return entityTypeHolder.value();
    }

    public int getCount() {
        return count;
    }

    public CompoundTag getData() {
        return data;
    }

    @Nullable
    public Entity getEntity() {
        return entity;
    }

    @Nullable
    public ItemStack getEgg() {
        var item = SpawnEggItem.byId(getEntityType());
        return item == null ? null : new ItemStack(item);
    }
}

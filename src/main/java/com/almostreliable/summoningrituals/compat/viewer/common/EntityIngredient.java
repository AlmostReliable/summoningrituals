package com.almostreliable.summoningrituals.compat.viewer.common;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.builder.SummoningEntityBuilder;
import com.almostreliable.summoningrituals.recipe.container.EntityInfo;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.storage.TagValueInput;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class EntityIngredient {

    public static final Codec<EntityIngredient> CODEC = RecordCodecBuilder.create(i -> i.group(
        BuiltInRegistries.ENTITY_TYPE.holderByNameCodec().fieldOf(Entity.TAG_ID).forGetter(e -> e.entityInfo.entity())
    ).apply(i, e -> new EntityIngredient(new SummoningEntityBuilder(e).build())));

    private final EntityInfo entityInfo;
    @Nullable
    private Entity entity;

    public EntityIngredient(EntityInfo entityInfo) {
        this.entityInfo = entityInfo;
        var level = Minecraft.getInstance().level;
        if (level != null) {
            entity = entityInfo.entity().value().create(level, EntitySpawnReason.LOAD);
            if (entity != null && entityInfo.data().isPresent()) {
                try (var problemReporter = new ProblemReporter.ScopedCollector(entity.problemPath(), SummoningRituals.LOGGER)) {
                    entity.load(TagValueInput.create(problemReporter, level.registryAccess(), entityInfo.data().get()));
                }
            }
        }
    }

    public Component getDisplayName() {
        if (entity == null) return Component.literal("Unknown Entity");
        return entity.getDisplayName();
    }

    @SuppressWarnings("BooleanParameter")
    public List<Component> getTooltip(boolean customTooltip, boolean advanced) {
        var tooltip = new ArrayList<Component>();
        if (entity == null) return tooltip;

        tooltip.add(getDisplayName());

        var entityTooltip = entityInfo.tooltip();
        if (customTooltip && !entityTooltip.isEmpty()) {
            tooltip.addAll(entityTooltip);
        }

        if (advanced) {
            var id = Component.literal(getId().toString());
            tooltip.add(id.withStyle(ChatFormatting.DARK_GRAY));
        }
        return tooltip;
    }

    public Identifier getId() {
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
        return SpawnEggItem.byId(entityInfo.entity().value()).map(ItemStack::new).orElse(null);
    }

    public EntityIngredient copy() {
        return new EntityIngredient(entityInfo);
    }
}

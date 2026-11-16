package com.almostreliable.summoningrituals.recipe.condition;

import com.almostreliable.summoningrituals.core.ModRegistration;
import com.almostreliable.summoningrituals.recipe.condition.handler.AltarTagBlockStateCondition;
import com.almostreliable.summoningrituals.recipe.condition.handler.ConditionHandler;
import com.almostreliable.summoningrituals.recipe.condition.handler.LocationCondition;
import com.almostreliable.summoningrituals.recipe.condition.handler.MoonPhaseCondition;
import com.almostreliable.summoningrituals.recipe.condition.handler.TimeCondition;
import com.almostreliable.summoningrituals.recipe.condition.handler.WeatherCondition;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

import com.mojang.serialization.MapCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConditionRegistry {

    public static final Map<MapCodec<? extends LootItemCondition>, ConditionHandler<?>> CONDITION_REGISTRY = new HashMap<>();

    private ConditionRegistry() {}

    public static void init() {
        CONDITION_REGISTRY.put(ModRegistration.ALTAR_TAG_BLOCK_STATE_CONDITION.get(), AltarTagBlockStateCondition.INSTANCE);
        CONDITION_REGISTRY.put(LocationCheck.MAP_CODEC, LocationCondition.INSTANCE);
        CONDITION_REGISTRY.put(ModRegistration.MOON_PHASE_CONDITION.get(), MoonPhaseCondition.INSTANCE);
        CONDITION_REGISTRY.put(TimeCheck.MAP_CODEC, TimeCondition.INSTANCE);
        CONDITION_REGISTRY.put(WeatherCheck.MAP_CODEC, WeatherCondition.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    public static <T extends LootItemCondition> ConditionHandler<T> getOrThrow(MapCodec<? extends LootItemCondition> type) {
        validate(type);
        return (ConditionHandler<T>) CONDITION_REGISTRY.get(type);
    }

    public static List<Component> getTooltip(LootItemCondition condition) {
        var tooltip = new ArrayList<Component>();
        var handler = getOrThrow(condition.codec());
        handler.getTooltip(tooltip, condition);
        return tooltip;
    }

    private static void validate(MapCodec<? extends LootItemCondition> type) {
        if (!CONDITION_REGISTRY.containsKey(type)) {
            throw new UnsupportedOperationException("missing condition handler for " + type);
        }
    }
}

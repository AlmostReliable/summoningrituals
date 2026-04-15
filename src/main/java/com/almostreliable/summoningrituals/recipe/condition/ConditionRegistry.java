package com.almostreliable.summoningrituals.recipe.condition;

import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.recipe.condition.handler.AltarTagBlockStateCondition;
import com.almostreliable.summoningrituals.recipe.condition.handler.ConditionHandler;
import com.almostreliable.summoningrituals.recipe.condition.handler.LocationCondition;
import com.almostreliable.summoningrituals.recipe.condition.handler.MoonPhaseCondition;
import com.almostreliable.summoningrituals.recipe.condition.handler.TimeCondition;
import com.almostreliable.summoningrituals.recipe.condition.handler.WeatherCondition;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConditionRegistry {

    public static final Map<LootItemConditionType, ConditionHandler<?>> CONDITION_REGISTRY = new HashMap<>();

    private ConditionRegistry() {}

    public static void init() {
        CONDITION_REGISTRY.put(Registration.ALTAR_TAG_BLOCK_STATE_CONDITION.get(), AltarTagBlockStateCondition.INSTANCE);
        CONDITION_REGISTRY.put(LootItemConditions.LOCATION_CHECK, LocationCondition.INSTANCE);
        CONDITION_REGISTRY.put(Registration.MOON_PHASE_CONDITION.get(), MoonPhaseCondition.INSTANCE);
        CONDITION_REGISTRY.put(LootItemConditions.TIME_CHECK, TimeCondition.INSTANCE);
        CONDITION_REGISTRY.put(LootItemConditions.WEATHER_CHECK, WeatherCondition.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    public static <T extends LootItemCondition> ConditionHandler<T> getOrThrow(LootItemConditionType type) {
        validate(type);
        return (ConditionHandler<T>) CONDITION_REGISTRY.get(type);
    }

    public static List<Component> getTooltip(LootItemCondition condition) {
        var tooltip = new ArrayList<Component>();
        var handler = getOrThrow(condition.getType());
        handler.getTooltip(tooltip, condition);
        return tooltip;
    }

    private static void validate(LootItemConditionType type) {
        if (!CONDITION_REGISTRY.containsKey(type)) {
            throw new UnsupportedOperationException("missing condition handler for " + type);
        }
    }
}

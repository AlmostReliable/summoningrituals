package com.almostreliable.summoningrituals.recipe.condition;

import com.almostreliable.summoningrituals.data.SummoningLang.LangEntry;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlockStateCondition implements ConditionHandler<LootItemBlockStatePropertyCondition> {

    public static final BlockStateCondition INSTANCE = new BlockStateCondition();
    private static final StreamCodec<RegistryFriendlyByteBuf, LootItemBlockStatePropertyCondition> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.holderRegistry(Registries.BLOCK), LootItemBlockStatePropertyCondition::block,
        ByteBufCodecs.optional(StatePropertiesPredicate.STREAM_CODEC), LootItemBlockStatePropertyCondition::properties,
        LootItemBlockStatePropertyCondition::new
    );

    private static final LangEntry BLOCK_STATE = LangEntry.condition("block_state", "Block Properties");

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, LootItemBlockStatePropertyCondition> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void getTooltip(List<Component> tooltip, LootItemBlockStatePropertyCondition condition) {
        var opt = condition.properties();
        if (opt.isEmpty()) return;

        var propertyTooltips = new ArrayList<Component>();
        var propertyPredicate = opt.get();
        var properties = propertyPredicate.properties();

        for (var prop : properties) {
            var propName = Component.literal(prop.name());
            var matcher = prop.valueMatcher();

            if (matcher instanceof StatePropertiesPredicate.ExactMatcher(var exactValue)) {
                propertyTooltips.add(conditionNamedValueComponent(propName, exactValue));
                continue;
            }

            if (matcher instanceof StatePropertiesPredicate.RangedMatcher(var minValueOpt, var maxValueOpt)) {
                var value = getRangedValueString(minValueOpt, maxValueOpt);
                if (value == null) continue;

                propertyTooltips.add(conditionNamedValueComponent(propName, value));
            }
        }

        if (propertyTooltips.isEmpty()) return;
        tooltip.add(conditionNameComponent(BLOCK_STATE.get()));
        tooltip.addAll(propertyTooltips);
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalIsPresent"})
    @Nullable
    private static String getRangedValueString(Optional<String> minValueOpt, Optional<String> maxValueOpt) {
        if (minValueOpt.isPresent() && maxValueOpt.isPresent()) {
            var minValue = minValueOpt.get();
            var maxValue = maxValueOpt.get();
            if (minValue.equals(maxValue)) {
                return minValue;
            }
            return minValue + " - " + maxValue;
        }

        if (minValueOpt.isPresent()) {
            return minValueOpt.get();
        }

        return maxValueOpt.orElse(null);
    }
}

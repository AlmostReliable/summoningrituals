package com.almostreliable.summoningrituals.recipe.condition;

import com.almostreliable.summoningrituals.data.SummoningLang.LangEntry;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface ConditionHandler<T extends LootItemCondition> {

    LangEntry YES = LangEntry.condition("yes", "Yes");
    LangEntry NO = LangEntry.condition("no", "No");
    LangEntry MINIMUM = LangEntry.condition("minimum", "Minimum");
    LangEntry MAXIMUM = LangEntry.condition("maximum", "Maximum");

    StreamCodec<RegistryFriendlyByteBuf, T> getStreamCodec();

    void getTooltip(List<Component> tooltip, T condition);

    default MutableComponent conditionNameComponent(Component name) {
        return Component.literal("- ").append(name).append(": ");
    }

    default MutableComponent conditionNamedValueComponent(Component name, Object value) {
        return Component.literal("> ").append(name).append(": ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(value.toString()).withStyle(ChatFormatting.AQUA));
    }

    default MutableComponent conditionValueComponent(Object value) {
        return Component.literal("> ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(value.toString()).withStyle(ChatFormatting.AQUA));
    }

    default MutableComponent conditionNameValueComponent(Component name, MutableComponent value) {
        return conditionNameComponent(name).append(value.withStyle(ChatFormatting.AQUA));
    }

    default MutableComponent conditionNameValueComponent(Component name, String value) {
        return conditionNameComponent(name).append(Component.literal(value).withStyle(ChatFormatting.AQUA));
    }

    default void addBlockStateTooltip(List<Component> tooltip, List<StatePropertiesPredicate.PropertyMatcher> properties) {
        for (var prop : properties) {
            var propName = Component.literal(prop.name());
            var matcher = prop.valueMatcher();

            if (matcher instanceof StatePropertiesPredicate.ExactMatcher(var exactValue)) {
                tooltip.add(conditionNamedValueComponent(propName, exactValue));
                continue;
            }

            if (matcher instanceof StatePropertiesPredicate.RangedMatcher(var minValueOpt, var maxValueOpt)) {
                var value = getRangedValueString(minValueOpt, maxValueOpt);
                if (value == null) continue;

                tooltip.add(conditionNamedValueComponent(propName, value));
            }
        }
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalIsPresent"})
    @Nullable
    default String getRangedValueString(Optional<String> minValueOpt, Optional<String> maxValueOpt) {
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

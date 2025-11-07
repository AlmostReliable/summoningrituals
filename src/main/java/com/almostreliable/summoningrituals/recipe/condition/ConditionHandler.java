package com.almostreliable.summoningrituals.recipe.condition;

import com.almostreliable.summoningrituals.data.SummoningLang.LangEntry;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

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
}

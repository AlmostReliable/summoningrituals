package com.almostreliable.summoningrituals.recipe.condition;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public interface ConditionHandler<T extends LootItemCondition> {

    StreamCodec<RegistryFriendlyByteBuf, T> getStreamCodec();

    void getTooltip(List<Component> tooltip, T condition);

    default Component conditionComponent(String name) {
        return Component.literal("- " + name + ":");
    }

    default Component conditionValueComponent(String name, Object value) {
        return Component.literal("> " + name + ": " + value).withStyle(ChatFormatting.GRAY);
    }
}

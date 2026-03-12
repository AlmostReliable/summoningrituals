package com.almostreliable.summoningrituals.recipe.condition;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;

import java.util.List;

public class AnyOfCompositeCondition implements ConditionHandler<AnyOfCondition> {

    public static final AnyOfCompositeCondition INSTANCE = new AnyOfCompositeCondition();
    private static final StreamCodec<RegistryFriendlyByteBuf, AnyOfCondition> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(
        CompositeLootItemCondition.createInlineCodec(AnyOfCondition::new));

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AnyOfCondition> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void getTooltip(List<Component> tooltip, AnyOfCondition condition) {
        var children = condition.terms;
        if (children.isEmpty()) return;

        // only delegate the call to the first child
        var first = children.getFirst();
        var handler = ConditionRegistry.getOrThrow(first.getType());
        handler.getTooltip(tooltip, first);
    }
}

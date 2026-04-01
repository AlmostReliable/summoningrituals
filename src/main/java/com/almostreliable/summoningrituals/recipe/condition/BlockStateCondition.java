package com.almostreliable.summoningrituals.recipe.condition;

import com.almostreliable.summoningrituals.data.SummoningLang;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;

import java.util.ArrayList;
import java.util.List;

public class BlockStateCondition implements ConditionHandler<LootItemBlockStatePropertyCondition> {

    public static final BlockStateCondition INSTANCE = new BlockStateCondition();
    private static final StreamCodec<RegistryFriendlyByteBuf, LootItemBlockStatePropertyCondition> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.holderRegistry(Registries.BLOCK), LootItemBlockStatePropertyCondition::block,
        ByteBufCodecs.optional(StatePropertiesPredicate.STREAM_CODEC), LootItemBlockStatePropertyCondition::properties,
        LootItemBlockStatePropertyCondition::new
    );

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
        addBlockStateTooltip(propertyTooltips, properties);

        if (propertyTooltips.isEmpty()) return;
        tooltip.add(conditionNameComponent(SummoningLang.ALTAR_PROPERTIES.get()));
        tooltip.addAll(propertyTooltips);
    }
}

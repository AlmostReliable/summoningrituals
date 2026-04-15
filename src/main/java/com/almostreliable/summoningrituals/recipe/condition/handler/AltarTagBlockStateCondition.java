package com.almostreliable.summoningrituals.recipe.condition.handler;

import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.recipe.condition.check.AltarTagBlockStateCheck;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public class AltarTagBlockStateCondition implements ConditionHandler<AltarTagBlockStateCheck> {

    public static final AltarTagBlockStateCondition INSTANCE = new AltarTagBlockStateCondition();

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AltarTagBlockStateCheck> getStreamCodec() {
        return AltarTagBlockStateCheck.STREAM_CODEC;
    }

    @Override
    public void getTooltip(List<Component> tooltip, AltarTagBlockStateCheck condition) {
        var propertyTooltips = new ArrayList<Component>();
        var propertyPredicate = condition.properties();
        var properties = propertyPredicate.properties();
        addBlockStateTooltip(propertyTooltips, properties);

        if (propertyTooltips.isEmpty()) return;
        tooltip.add(conditionNameComponent(SummoningLang.ALTAR_PROPERTIES.get()));
        tooltip.addAll(propertyTooltips);
    }
}

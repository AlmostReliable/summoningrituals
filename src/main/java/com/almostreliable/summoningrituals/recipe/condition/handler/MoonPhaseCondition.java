package com.almostreliable.summoningrituals.recipe.condition.handler;

import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.recipe.condition.check.MoonPhaseCheck;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public class MoonPhaseCondition implements ConditionHandler<MoonPhaseCheck> {

    public static final MoonPhaseCondition INSTANCE = new MoonPhaseCondition();

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MoonPhaseCheck> getStreamCodec() {
        return MoonPhaseCheck.STREAM_CODEC;
    }

    @Override
    public void getTooltip(List<Component> tooltip, MoonPhaseCheck condition) {
        var moonPhase = condition.phase();
        var description = SummoningLang.MOON_PHASES.get(moonPhase);
        if (description != null) {
            tooltip.add(conditionNameValueComponent(SummoningLang.MOON_PHASE.get(), description.get()));
        }
    }
}

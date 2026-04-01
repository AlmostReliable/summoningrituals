package com.almostreliable.summoningrituals.recipe.condition.custom;

import com.almostreliable.summoningrituals.core.Registration;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MoonPhaseCheck(MoonPhase phase) implements LootItemCondition {

    private static final MoonPhase[] PHASES = MoonPhase.values();
    public static final MapCodec<MoonPhaseCheck> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.fieldOf("phase").forGetter(check -> check.phase.ordinal())
    ).apply(i, phase -> new MoonPhaseCheck(PHASES[phase])));
    public static final StreamCodec<RegistryFriendlyByteBuf, MoonPhaseCheck> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, check -> check.phase.ordinal(),
        phase -> new MoonPhaseCheck(PHASES[phase])
    );

    @Override
    public LootItemConditionType getType() {
        return Registration.MOON_PHASE_CONDITION.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        var level = lootContext.getLevel();
        var moonPhaseOrdinal = level.getMoonPhase();
        return moonPhaseOrdinal == phase.ordinal();
    }

    public enum MoonPhase {
        FULL_MOON,
        WANING_GIBBOUS,
        THIRD_QUARTER,
        WANING_CRESCENT,
        NEW_MOON,
        WAXING_CRESCENT,
        FIRST_QUARTER,
        WAXING_GIBBOUS
    }
}

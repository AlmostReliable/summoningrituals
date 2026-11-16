package com.almostreliable.summoningrituals.recipe.condition.check;

import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.core.ModRegistration;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MoonPhaseCheck(MoonPhase phase) implements LootItemCondition {

    private static final MoonPhase[] PHASES = MoonPhase.values();
    public static final MapCodec<MoonPhaseCheck> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.fieldOf(Constants.MOON_PHASE).forGetter(check -> check.phase.index())
    ).apply(i, phase -> new MoonPhaseCheck(PHASES[phase])));
    public static final StreamCodec<RegistryFriendlyByteBuf, MoonPhaseCheck> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, check -> check.phase.index(),
        phase -> new MoonPhaseCheck(PHASES[phase])
    );

    @Override
    public MapCodec<MoonPhaseCheck> codec() {
        return ModRegistration.MOON_PHASE_CONDITION.get();
    }

    @SuppressWarnings("resource")
    @Override
    public boolean test(LootContext lootContext) {
        var level = lootContext.getLevel();
        var origin = lootContext.getOptionalParameter(LootContextParams.ORIGIN);
        if (origin == null) {
            origin = Vec3.ZERO;
        }

        // only the overworld has the moon phase timeline, other dimensions always report the full moon default
        var overworld = level.getServer().overworld();
        var moonPhase = overworld.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, origin);

        return moonPhase == phase;
    }
}

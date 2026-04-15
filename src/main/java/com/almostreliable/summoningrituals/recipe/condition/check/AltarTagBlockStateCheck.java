package com.almostreliable.summoningrituals.recipe.condition.check;

import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.data.SummoningTags;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AltarTagBlockStateCheck(StatePropertiesPredicate properties) implements LootItemCondition {

    public static final MapCodec<AltarTagBlockStateCheck> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        StatePropertiesPredicate.CODEC.fieldOf(Constants.PROPERTIES).forGetter(AltarTagBlockStateCheck::properties)
    ).apply(i, AltarTagBlockStateCheck::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AltarTagBlockStateCheck> STREAM_CODEC = StreamCodec.composite(
        StatePropertiesPredicate.STREAM_CODEC, AltarTagBlockStateCheck::properties,
        AltarTagBlockStateCheck::new
    );

    @Override
    public LootItemConditionType getType() {
        return Registration.ALTAR_TAG_BLOCK_STATE_CONDITION.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        var blockstate = lootContext.getParamOrNull(LootContextParams.BLOCK_STATE);
        return blockstate != null && blockstate.is(SummoningTags.ALTARS) && properties.matches(blockstate);
    }
}

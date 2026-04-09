package com.almostreliable.summoningrituals.recipe.input;

import com.almostreliable.summoningrituals.core.Constants;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public record FakeEntityInput(ItemStack displayItem, Optional<Integer> count, Predicate<Entity> predicate) implements BaseEntityInput {

    public static final Codec<FakeEntityInput> CODEC = RecordCodecBuilder.create(i -> i.group(
        ItemStack.STRICT_CODEC.fieldOf(Constants.ITEM).forGetter(FakeEntityInput::displayItem),
        Codec.INT.optionalFieldOf(Constants.COUNT).forGetter(FakeEntityInput::count)
    ).apply(i, FakeEntityInput::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FakeEntityInput> STREAM_CODEC = StreamCodec.composite(
        ItemStack.STREAM_CODEC, FakeEntityInput::displayItem,
        ByteBufCodecs.optional(ByteBufCodecs.INT), FakeEntityInput::count,
        FakeEntityInput::new
    );

    // only used for the client; on the server, a fake entity must have a validator
    private FakeEntityInput(ItemStack displayItem, Optional<Integer> count) {
        this(displayItem, count, $ -> true);
    }
}

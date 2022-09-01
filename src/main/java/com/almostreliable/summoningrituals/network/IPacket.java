package com.almostreliable.summoningrituals.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.function.Supplier;

public interface IPacket<T> {
    void encode(T packet, FriendlyByteBuf buffer);

    T decode(FriendlyByteBuf buffer);

    void handle(T packet, Supplier<? extends Context> context);
}

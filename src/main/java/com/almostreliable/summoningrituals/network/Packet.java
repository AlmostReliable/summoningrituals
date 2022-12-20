package com.almostreliable.summoningrituals.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface Packet<T> {
    void encode(T packet, FriendlyByteBuf buffer);

    T decode(FriendlyByteBuf buffer);

    void handle(T packet, Supplier<? extends NetworkEvent.Context> context);
}

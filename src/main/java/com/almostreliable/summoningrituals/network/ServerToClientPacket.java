package com.almostreliable.summoningrituals.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.function.Supplier;

public abstract class ServerToClientPacket<T> implements IPacket<T> {

    @Override
    public void handle(T packet, Supplier<? extends Context> context) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        context.get().enqueueWork(() -> handlePacket(packet, level));
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    protected abstract void handlePacket(T packet, ClientLevel level);
}

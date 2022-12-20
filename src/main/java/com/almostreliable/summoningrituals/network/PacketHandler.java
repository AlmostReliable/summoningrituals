package com.almostreliable.summoningrituals.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class PacketHandler {

    private PacketHandler() {}

    public static void initS2C() {
        ClientPlayNetworking.registerGlobalReceiver(
            ClientAltarUpdatePacket.CHANNEL, ClientAltarUpdatePacket::handlePacket
        );
        ClientPlayNetworking.registerGlobalReceiver(
            SacrificeParticlePacket.CHANNEL, SacrificeParticlePacket::handlePacket
        );
    }
}

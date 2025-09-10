package com.almostreliable.summoningrituals.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public final class PacketHandler {

    private static final String PROTOCOL = "1";

    private PacketHandler() {}

    public static void init(IEventBus eventBus) {
        eventBus.addListener(PacketHandler::onPacketRegistration);
    }

    private static void onPacketRegistration(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL);

        // server to client
        registrar.playToClient(AltarSyncPacket.TYPE, AltarSyncPacket.STREAM_CODEC, wrapHandler(AltarSyncPacket::handle));
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> wrapHandler(IPayloadHandler<T> handler) {
        return (payload, context) -> context.enqueueWork(() -> handler.handle(payload, context));
    }
}

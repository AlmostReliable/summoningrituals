package com.almostreliable.summoningrituals.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
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
        registrar.playToClient(
            AltarInventorySyncPacket.TYPE,
            AltarInventorySyncPacket.STREAM_CODEC,
            wrapHandler(AltarInventorySyncPacket::handle)
        );
        registrar.playToClient(
            AltarRecipeSyncPacket.TYPE,
            AltarRecipeSyncPacket.STREAM_CODEC,
            wrapHandler(AltarRecipeSyncPacket::handle)
        );
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> wrapHandler(IPayloadHandler<T> handler) {
        return (payload, context) -> context.enqueueWork(() -> handler.handle(payload, context));
    }

    public static void sendToTrackingChunk(ServerLevel level, BlockPos pos, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayersTrackingChunk(level, level.getChunkAt(pos).getPos(), packet);
    }
}

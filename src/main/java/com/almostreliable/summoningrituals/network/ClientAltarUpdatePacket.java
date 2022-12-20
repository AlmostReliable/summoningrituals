package com.almostreliable.summoningrituals.network;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.util.Utils;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class ClientAltarUpdatePacket {

    static final ResourceLocation CHANNEL = Utils.getRL("process_time");

    private ClientAltarUpdatePacket() {}

    @SuppressWarnings("unused")
    static void handlePacket(
        Minecraft mc, ClientPacketListener packetListener, FriendlyByteBuf buffer, PacketSender packetSender
    ) {
        if (mc.level == null || !(mc.level.getBlockEntity(buffer.readBlockPos()) instanceof AltarBlockEntity altar)) {
            return;
        }
        var type = buffer.readEnum(PacketType.class);
        if (type == PacketType.PROGRESS) {
            altar.progress = buffer.readInt();
        } else if (type == PacketType.PROCESS_TIME) {
            altar.processTime = buffer.readInt();
        } else {
            throw new IllegalStateException("Unknown packet type: " + type);
        }
    }

    private static FriendlyByteBuf encode(BlockPos pos, int value, PacketType type) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeBlockPos(pos);
        buffer.writeEnum(type);
        buffer.writeInt(value);
        return buffer;
    }

    public static void progressUpdate(BlockPos pos, int progress) {
        ClientPlayNetworking.send(CHANNEL, encode(pos, progress, PacketType.PROGRESS));
    }

    public static void processTimeUpdate(BlockPos pos, int processTime) {
        ClientPlayNetworking.send(CHANNEL, encode(pos, processTime, PacketType.PROCESS_TIME));
    }

    public enum PacketType {
        PROGRESS,
        PROCESS_TIME
    }
}

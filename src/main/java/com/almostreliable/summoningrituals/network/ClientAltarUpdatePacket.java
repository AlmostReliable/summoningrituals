package com.almostreliable.summoningrituals.network;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ClientAltarUpdatePacket extends ServerToClientPacket<ClientAltarUpdatePacket> {

    private BlockPos pos;
    private PacketType type;
    private int value;

    private ClientAltarUpdatePacket(BlockPos pos, PacketType type, int value) {
        this.pos = pos;
        this.type = type;
        this.value = value;
    }

    ClientAltarUpdatePacket() {}

    public static ClientAltarUpdatePacket progressUpdate(BlockPos pos, int progress) {
        return new ClientAltarUpdatePacket(pos, PacketType.PROGRESS, progress);
    }

    public static ClientAltarUpdatePacket processTimeUpdate(BlockPos pos, int processTime) {
        return new ClientAltarUpdatePacket(pos, PacketType.PROCESS_TIME, processTime);
    }

    @Override
    public void encode(ClientAltarUpdatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeEnum(packet.type);
        buffer.writeInt(packet.value);
    }

    @Override
    public ClientAltarUpdatePacket decode(FriendlyByteBuf buffer) {
        return new ClientAltarUpdatePacket(
            buffer.readBlockPos(),
            buffer.readEnum(PacketType.class),
            buffer.readInt()
        );
    }

    @Override
    protected void handlePacket(ClientAltarUpdatePacket packet, ClientLevel level) {
        if (!(level.getBlockEntity(packet.pos) instanceof AltarBlockEntity altar)) {
            return;
        }
        if (type == PacketType.PROGRESS) {
            altar.progress = packet.value;
        } else if (type == PacketType.PROCESS_TIME) {
            altar.processTime = packet.value;
        } else {
            throw new IllegalStateException("Unknown packet type: " + type);
        }
    }

    public enum PacketType {
        PROGRESS,
        PROCESS_TIME
    }
}

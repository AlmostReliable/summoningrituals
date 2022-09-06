package com.almostreliable.summoningrituals.network.packet;

import com.almostreliable.summoningrituals.network.ClientHandler;
import com.almostreliable.summoningrituals.network.ServerToClientPacket;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ProcessTimeUpdatePacket extends ServerToClientPacket<ProcessTimeUpdatePacket> {

    private final BlockPos pos;
    private final int processTime;

    public ProcessTimeUpdatePacket(BlockPos pos, int processTime) {
        this.pos = pos;
        this.processTime = processTime;
    }

    public ProcessTimeUpdatePacket() {
        this(new BlockPos(0, 0, 0), 0);
    }

    @Override
    public void encode(ProcessTimeUpdatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.processTime);
    }

    @Override
    public ProcessTimeUpdatePacket decode(FriendlyByteBuf buffer) {
        return new ProcessTimeUpdatePacket(buffer.readBlockPos(), buffer.readInt());
    }

    @Override
    protected void handlePacket(ProcessTimeUpdatePacket packet, ClientLevel level) {
        ClientHandler.handleProcessTimeUpdate(packet, level);
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getProcessTime() {
        return processTime;
    }
}

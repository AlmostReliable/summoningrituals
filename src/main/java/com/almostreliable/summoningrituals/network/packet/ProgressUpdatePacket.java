package com.almostreliable.summoningrituals.network.packet;

import com.almostreliable.summoningrituals.altar.AltarEntity;
import com.almostreliable.summoningrituals.network.ServerToClientPacket;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ProgressUpdatePacket extends ServerToClientPacket<ProgressUpdatePacket> {

    private final BlockPos pos;
    private final int progress;

    public ProgressUpdatePacket(BlockPos pos, int progress) {
        this.pos = pos;
        this.progress = progress;
    }

    public ProgressUpdatePacket() {
        this(new BlockPos(0, 0, 0), 0);
    }

    @Override
    public void encode(ProgressUpdatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.progress);
    }

    @Override
    public ProgressUpdatePacket decode(FriendlyByteBuf buffer) {
        return new ProgressUpdatePacket(buffer.readBlockPos(), buffer.readInt());
    }

    @Override
    protected void handlePacket(ProgressUpdatePacket packet, ClientLevel level) {
        if (!(level.getBlockEntity(packet.pos) instanceof AltarEntity altar)) return;
        altar.setProgress(packet.progress);
    }
}

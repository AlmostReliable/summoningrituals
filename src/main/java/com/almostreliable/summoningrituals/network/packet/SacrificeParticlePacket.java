package com.almostreliable.summoningrituals.network.packet;

import com.almostreliable.summoningrituals.network.ClientHandler;
import com.almostreliable.summoningrituals.network.ServerToClientPacket;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.stream.IntStream;

public class SacrificeParticlePacket extends ServerToClientPacket<SacrificeParticlePacket> {

    private final List<BlockPos> positions;

    public SacrificeParticlePacket(List<BlockPos> positions) {
        this.positions = positions;
    }

    public SacrificeParticlePacket() {
        this(List.of());
    }

    @Override
    public void encode(SacrificeParticlePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.positions.size());
        for (BlockPos pos : packet.positions) {
            buffer.writeBlockPos(pos);
        }
    }

    @Override
    public SacrificeParticlePacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        return new SacrificeParticlePacket(IntStream.range(0, size)
            .mapToObj(i -> buffer.readBlockPos())
            .toList());
    }

    @Override
    protected void handlePacket(SacrificeParticlePacket packet, ClientLevel level) {
        ClientHandler.handleSacrificeParticle(packet, level);
    }

    public List<BlockPos> getPositions() {
        return positions;
    }
}

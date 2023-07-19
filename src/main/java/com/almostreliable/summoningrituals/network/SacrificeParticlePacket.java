package com.almostreliable.summoningrituals.network;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class SacrificeParticlePacket extends ServerToClientPacket<SacrificeParticlePacket> {

    private List<BlockPos> positions;

    public SacrificeParticlePacket(List<BlockPos> positions) {
        this.positions = positions;
    }

    SacrificeParticlePacket() {}

    @Override
    public void encode(SacrificeParticlePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.positions.size());
        packet.positions.forEach(buffer::writeBlockPos);
    }

    @Override
    public SacrificeParticlePacket decode(FriendlyByteBuf buffer) {
        return new SacrificeParticlePacket(
            IntStream.range(0, buffer.readVarInt())
                .mapToObj(i -> buffer.readBlockPos())
                .toList()
        );
    }

    @Override
    protected void handlePacket(SacrificeParticlePacket packet, ClientLevel level) {
        var random = new Random();
        for (var pos : packet.positions) {
            for (var i = 0; i < 10; i++) {
                level.addParticle(
                    ParticleTypes.SOUL,
                    pos.getX() + random.nextFloat(),
                    pos.getY() + random.nextFloat(),
                    pos.getZ() + random.nextFloat(),
                    0,
                    0.05f,
                    0
                );
            }
        }
    }
}

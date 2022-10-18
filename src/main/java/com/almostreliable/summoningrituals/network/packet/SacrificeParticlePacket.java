package com.almostreliable.summoningrituals.network.packet;

import com.almostreliable.summoningrituals.network.ServerToClientPacket;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.Random;
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

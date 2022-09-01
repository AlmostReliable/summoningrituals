package com.almostreliable.summoningrituals.network;

import com.almostreliable.summoningrituals.altar.AltarEntity;
import com.almostreliable.summoningrituals.network.packet.ProgressUpdatePacket;
import com.almostreliable.summoningrituals.network.packet.SacrificeParticlePacket;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;

import java.util.Random;

public final class ClientHandler {

    private ClientHandler() {}

    public static void handleProgressUpdate(ProgressUpdatePacket packet, ClientLevel level) {
        if (!(level.getBlockEntity(packet.getPos()) instanceof AltarEntity altar)) return;
        altar.setProgress(packet.getProgress());
    }

    public static void handleSacrificeParticle(SacrificeParticlePacket packet, ClientLevel level) {
        var random = new Random();
        for (var pos : packet.getPositions()) {
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

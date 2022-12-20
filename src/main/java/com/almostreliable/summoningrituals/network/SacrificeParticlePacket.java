package com.almostreliable.summoningrituals.network;

import com.almostreliable.summoningrituals.util.Utils;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public final class SacrificeParticlePacket {

    public static final ResourceLocation CHANNEL = Utils.getRL("sacrifice_particle");

    private SacrificeParticlePacket() {}

    @SuppressWarnings("unused")
    static void handlePacket(
        Minecraft mc, ClientPacketListener packetListener, FriendlyByteBuf buffer, PacketSender packetSender
    ) {
        if (mc.level == null) return;
        var size = buffer.readVarInt();
        var pos = IntStream.range(0, size).mapToObj(i -> buffer.readBlockPos()).toList();
        var random = new Random();
        for (var p : pos) {
            for (var i = 0; i < 10; i++) {
                mc.level.addParticle(
                    ParticleTypes.SOUL,
                    p.x + random.nextFloat(),
                    p.y + random.nextFloat(),
                    p.z + random.nextFloat(),
                    0,
                    0.05f,
                    0
                );
            }
        }
    }

    public static FriendlyByteBuf encode(List<BlockPos> pos) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeVarInt(pos.size());
        for (BlockPos blockPos : pos) {
            buffer.writeBlockPos(blockPos);
        }
        return buffer;
    }
}

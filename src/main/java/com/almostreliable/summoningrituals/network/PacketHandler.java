package com.almostreliable.summoningrituals.network;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.network.packet.ProcessTimeUpdatePacket;
import com.almostreliable.summoningrituals.network.packet.ProgressUpdatePacket;
import com.almostreliable.summoningrituals.network.packet.SacrificeParticlePacket;
import com.almostreliable.summoningrituals.util.TextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PacketHandler {

    private static final ResourceLocation ID = TextUtils.getRL(Constants.NETWORK);
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(ID)
        .networkProtocolVersion(() -> PROTOCOL)
        .clientAcceptedVersions(PROTOCOL::equals)
        .serverAcceptedVersions(PROTOCOL::equals)
        .simpleChannel();

    private PacketHandler() {}

    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public static void init() {
        var packetId = -1;
        // server to client
        register(++packetId, ProgressUpdatePacket.class, new ProgressUpdatePacket());
        register(++packetId, ProcessTimeUpdatePacket.class, new ProcessTimeUpdatePacket());
        register(++packetId, SacrificeParticlePacket.class, new SacrificeParticlePacket());
    }

    private static <T> void register(int packetId, Class<T> clazz, IPacket<T> packet) {
        CHANNEL.registerMessage(packetId, clazz, packet::encode, packet::decode, packet::handle);
    }
}

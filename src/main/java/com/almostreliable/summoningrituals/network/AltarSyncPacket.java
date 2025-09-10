package com.almostreliable.summoningrituals.network;

import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.util.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AltarSyncPacket(BlockPos altarPos, int recipeProgress, int recipeTime) implements CustomPacketPayload {

    static final Type<AltarSyncPacket> TYPE = new Type<>(Utils.getRL("altar_sync"));

    static final StreamCodec<FriendlyByteBuf, AltarSyncPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, AltarSyncPacket::altarPos,
        ByteBufCodecs.VAR_INT, AltarSyncPacket::recipeProgress,
        ByteBufCodecs.VAR_INT, AltarSyncPacket::recipeTime,
        AltarSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AltarSyncPacket packet, IPayloadContext ctx) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        level.getBlockEntity(packet.altarPos, Registration.ALTAR_BLOCK_ENTITY.get()).ifPresent(altar -> {
            altar.setRecipeProgress(packet.recipeProgress);
            altar.setRecipeTime(packet.recipeTime);
        });
    }
}

package com.almostreliable.summoningrituals.network;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.client.render.BlockHighlightRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record HighlightPositionsPacket(List<BlockPos> positions) implements CustomPacketPayload {

    static final Type<HighlightPositionsPacket> TYPE = new Type<>(SummoningRituals.getRL("highlight_positions"));
    static final StreamCodec<FriendlyByteBuf, HighlightPositionsPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), HighlightPositionsPacket::positions,
        HighlightPositionsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static void handle(HighlightPositionsPacket packet, IPayloadContext ignoredCtx) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var gameTime = level.getGameTime();
        BlockHighlightRenderer.highlightWrongPositions(gameTime, packet.positions);
    }
}

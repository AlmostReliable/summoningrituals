package com.almostreliable.summoningrituals.network;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.core.Registration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AltarRecipeSyncPacket(BlockPos altarPos, int recipeProgress, int recipeTime) implements CustomPacketPayload {

    static final Type<AltarRecipeSyncPacket> TYPE = new Type<>(SummoningRituals.getRL("altar_recipe_sync"));

    static final StreamCodec<FriendlyByteBuf, AltarRecipeSyncPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        AltarRecipeSyncPacket::altarPos,
        ByteBufCodecs.VAR_INT,
        AltarRecipeSyncPacket::recipeProgress,
        ByteBufCodecs.VAR_INT,
        AltarRecipeSyncPacket::recipeTime,
        AltarRecipeSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AltarRecipeSyncPacket packet, IPayloadContext ignoredCtx) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        level.getBlockEntity(packet.altarPos, Registration.ALTAR_BLOCK_ENTITY.get()).ifPresent(altar -> {
            altar.setRecipeProgress(packet.recipeProgress);
            altar.setRecipeTime(packet.recipeTime);
        });
    }
}

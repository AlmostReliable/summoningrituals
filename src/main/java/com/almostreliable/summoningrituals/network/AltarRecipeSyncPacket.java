package com.almostreliable.summoningrituals.network;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.recipe.container.RecipeInfoContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record AltarRecipeSyncPacket(
    BlockPos altarPos, Optional<RecipeInfoContainer> recipeInfo, int recipeProgress, int recipeTime
) implements CustomPacketPayload {

    static final Type<AltarRecipeSyncPacket> TYPE = new Type<>(SummoningRituals.getRL("altar_recipe_sync"));

    static final StreamCodec<RegistryFriendlyByteBuf, AltarRecipeSyncPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        AltarRecipeSyncPacket::altarPos,
        ByteBufCodecs.optional(RecipeInfoContainer.STREAM_CODEC),
        AltarRecipeSyncPacket::recipeInfo,
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
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var blockEntity = level.getBlockEntity(packet.altarPos);
        if (blockEntity instanceof AltarBlockEntity altar) {
            altar.setRecipeProgress(packet.recipeProgress);
            var recipeTime = packet.recipeTime;
            altar.setRecipeTime(recipeTime);
            if (recipeTime > 0) {
                packet.recipeInfo.ifPresent(altar::setCurrentRecipeInfo);
            } else {
                altar.setCurrentRecipeInfo(null);
            }
        }
    }
}

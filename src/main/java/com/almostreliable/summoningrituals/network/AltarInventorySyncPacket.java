package com.almostreliable.summoningrituals.network;

import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.util.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AltarInventorySyncPacket(BlockPos altarPos, CompoundTag inventoryData) implements CustomPacketPayload {

    static final Type<AltarInventorySyncPacket> TYPE = new Type<>(Utils.getRL("altar_inventory_sync"));

    static final StreamCodec<FriendlyByteBuf, AltarInventorySyncPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, AltarInventorySyncPacket::altarPos,
        ByteBufCodecs.COMPOUND_TAG, AltarInventorySyncPacket::inventoryData,
        AltarInventorySyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AltarInventorySyncPacket packet, IPayloadContext ignoredCtx) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        level.getBlockEntity(packet.altarPos, Registration.ALTAR_BLOCK_ENTITY.get()).ifPresent(
            altar -> altar.getInventory().deserializeNBT(level.registryAccess(), packet.inventoryData())
        );
    }
}

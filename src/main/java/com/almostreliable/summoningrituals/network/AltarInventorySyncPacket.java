package com.almostreliable.summoningrituals.network;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.core.ModRegistration;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AltarInventorySyncPacket(BlockPos altarPos, CompoundTag updateTag) implements CustomPacketPayload {

    static final Type<AltarInventorySyncPacket> TYPE = new Type<>(SummoningRituals.getRL("altar_inventory_sync"));
    static final StreamCodec<FriendlyByteBuf, AltarInventorySyncPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        AltarInventorySyncPacket::altarPos,
        ByteBufCodecs.COMPOUND_TAG,
        AltarInventorySyncPacket::updateTag,
        AltarInventorySyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static void handle(AltarInventorySyncPacket packet, IPayloadContext ignoredCtx) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var altar = level.getBlockEntity(packet.altarPos, ModRegistration.ALTAR_BLOCK_ENTITY.get()).orElse(null);
        if (altar == null) return;

        try (var problemReporter = new ProblemReporter.ScopedCollector(altar.problemPath(), SummoningRituals.LOGGER)) {
            var input = TagValueInput.create(problemReporter, level.registryAccess(), packet.updateTag);
            altar.loadCustomOnly(input);
        }
    }
}

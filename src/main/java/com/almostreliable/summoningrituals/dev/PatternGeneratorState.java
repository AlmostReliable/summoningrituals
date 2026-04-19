package com.almostreliable.summoningrituals.dev;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

import java.util.Optional;

public record PatternGeneratorState(Optional<BlockPos> pos1, Optional<BlockPos> pos2) {

    public static final StreamCodec<FriendlyByteBuf, PatternGeneratorState> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC), PatternGeneratorState::pos1,
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC), PatternGeneratorState::pos2,
        PatternGeneratorState::new
    );
    public static final PatternGeneratorState DEFAULT = new PatternGeneratorState(Optional.empty(), Optional.empty());

    public PatternGeneratorState recordPos(BlockPos pos) {
        if (pos1.isEmpty()) return new PatternGeneratorState(Optional.of(pos), pos2);
        if (pos2.isEmpty()) return new PatternGeneratorState(pos1, Optional.of(pos));
        return DEFAULT.recordPos(pos);
    }

    public String getMessage() {
        if (pos2.isPresent()) return "Position 2 set!";
        return "Position 1 set!";
    }

    public boolean isComplete() {
        return pos1.isPresent() && pos2.isPresent();
    }

    public AABB getBounds() {
        var p1 = pos1.orElse(BlockPos.ZERO);
        var p2 = pos2.orElse(BlockPos.ZERO);
        return new AABB(p1.getX(), p1.getY(), p1.getZ(), p2.getX(), p2.getY(), p2.getZ());
    }

    public AABB getRenderBounds() {
        var p1 = pos1.orElse(BlockPos.ZERO);

        if (pos2.isPresent()) {
            var p2 = pos2.orElse(BlockPos.ZERO);
            return AABB.encapsulatingFullBlocks(p1, p2);
        }

        return new AABB(p1);
    }
}

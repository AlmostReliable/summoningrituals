package com.almostreliable.summoningrituals.recipe.component;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nullable;

public final class BlockReference {

    public static Codec<BlockReference> CODEC = BlockPredicate.CODEC.xmap(
        BlockReference::new,
        blockReference -> blockReference.blockPredicate
    );

    private final BlockPredicate blockPredicate;

    @Nullable
    private BlockState displayState;

    private BlockReference(BlockPredicate blockPredicate) {
        this.blockPredicate = blockPredicate;
    }

    public boolean test(Level level, BlockPos pos) {
        return blockPredicate.matches(new BlockInWorld(level, pos, false));
    }

    public BlockState getDisplayState() {
        throw new NotImplementedException("Not implemented");
        // if (displayState != null) return displayState;
        //
        // AtomicReference<BlockState> newState = new AtomicReference<>(block.defaultBlockState());
        // for (Property<?> property : newState.get().getProperties()) {
        //     Object newValue = properties.get(property.getName());
        //     if (newValue == null) continue;
        //     try {
        //         newState.set(newState.get().setValue(property, Utils.cast(newValue)));
        //     } catch (Exception ignored) {
        //         property.getValue(newValue.toString())
        //             .ifPresent(v -> newState.set(newState.get().setValue(property, Utils.cast(v))));
        //     }
        // }
        // displayState = newState.get();
        // return displayState;
    }
}

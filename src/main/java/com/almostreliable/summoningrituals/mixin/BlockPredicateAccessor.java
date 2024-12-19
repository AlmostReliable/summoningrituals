package com.almostreliable.summoningrituals.mixin;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.throwables.MixinException;

import javax.annotation.Nullable;

@Mixin(BlockPredicate.class)
public interface BlockPredicateAccessor {

    @Invoker("matchesBlockEntity")
    static boolean summoningrituals$matchesBlockEntity(
        LevelReader level, @Nullable BlockEntity blockEntity, NbtPredicate nbtPredicate
    ) {
        throw new MixinException("Invoker not transformed.");
    }

    @Invoker("matchesState")
    boolean summoningrituals$matchesState(BlockState state);
}

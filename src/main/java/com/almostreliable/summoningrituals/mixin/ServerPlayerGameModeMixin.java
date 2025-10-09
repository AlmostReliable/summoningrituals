package com.almostreliable.summoningrituals.mixin;

import com.almostreliable.summoningrituals.altar.AltarBlock;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    @Inject(
        method = "useItemOn",
        at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;")
    )
    private void summoning$useItemOn(
        ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult,
        CallbackInfoReturnable<InteractionResult> cir, @Local BlockState blockState, @Local PlayerInteractEvent.RightClickBlock event,
        @Local(ordinal = 0) boolean hasItem, @Local(ordinal = 1) boolean isSecondaryActive, @Local(ordinal = 1) ItemStack itemStack
    ) {
        if (hand != InteractionHand.MAIN_HAND || !hasItem || !isSecondaryActive || itemStack.getItem() instanceof BlockItem) {
            return;
        }

        if (blockState.getBlock() instanceof AltarBlock) {
            event.setUseBlock(TriState.TRUE);
        }
    }
}


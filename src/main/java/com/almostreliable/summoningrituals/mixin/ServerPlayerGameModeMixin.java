package com.almostreliable.summoningrituals.mixin;

import com.almostreliable.summoningrituals.altar.AltarBlock;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Inject(
        method = "useItemOn",
        at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    private void summoning$useItemOn(
        ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult,
        CallbackInfoReturnable<InteractionResult> cir, BlockPos blockPos, BlockState blockState,
        PlayerInteractEvent.RightClickBlock event, UseOnContext useOnContext,
        boolean hasItem, boolean isSecondaryActive, ItemStack itemStack
    ) {
        if (hand != InteractionHand.MAIN_HAND || !hasItem || !isSecondaryActive || itemStack.getItem() instanceof BlockItem) {
            return;
        }

        if (blockState.getBlock() instanceof AltarBlock) {
            InteractionResult interactionResult = blockState.use(level, player, hand, hitResult);
            if (interactionResult.consumesAction()) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, blockPos, itemStack);
                cir.setReturnValue(interactionResult);
            }
        }
    }
}

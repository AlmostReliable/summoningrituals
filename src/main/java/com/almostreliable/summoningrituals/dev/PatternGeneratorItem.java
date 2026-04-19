package com.almostreliable.summoningrituals.dev;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.altar.AltarBlock;
import com.almostreliable.summoningrituals.client.render.BlockHighlightRenderer;
import com.almostreliable.summoningrituals.core.Registration;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PatternGeneratorItem extends Item {

    public PatternGeneratorItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("Pattern Generator (Dev)");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide || usedHand != InteractionHand.MAIN_HAND || !Screen.hasShiftDown()) {
            return InteractionResultHolder.pass(player.getItemInHand(usedHand));
        }

        var stack = player.getMainHandItem();
        resetPositions(stack, player);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var player = context.getPlayer();
        if (!(level instanceof ClientLevel clientLevel) || player == null || context.getHand() != InteractionHand.MAIN_HAND) {
            return super.useOn(context);
        }

        var stack = context.getItemInHand();
        if (Screen.hasShiftDown()) {
            resetPositions(stack, player);
            return InteractionResult.SUCCESS;
        }

        var pos = context.getClickedPos();
        var blockState = level.getBlockState(pos);
        if (blockState.isAir()) return InteractionResult.PASS;

        var state = stack.getOrDefault(Registration.PATTERN_GENERATOR_COMPONENT, PatternGeneratorState.DEFAULT);

        if (blockState.is(Registration.ALTAR_BLOCK)) {
            if (blockState.getValue(AltarBlock.FACING) != Direction.NORTH) {
                player.displayClientMessage(Component.literal("Must be facing north!").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            if (state.isComplete()) {
                var bounds = state.getBounds();
                if (!bounds.contains(Vec3.atLowerCornerOf(pos))) {
                    player.displayClientMessage(Component.literal("Must be inside bounds!").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }

                var count = generateAndPrintPattern(clientLevel, pos, bounds);
                player.displayClientMessage(
                    Component.literal("Generated pattern with " + count + " blocks.")
                        .withStyle(ChatFormatting.GREEN), true
                );
                stack.set(Registration.PATTERN_GENERATOR_COMPONENT, PatternGeneratorState.DEFAULT);

                return InteractionResult.SUCCESS;
            }

            player.displayClientMessage(Component.literal("Two positions required!").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        var newState = state.recordPos(pos);
        stack.set(Registration.PATTERN_GENERATOR_COMPONENT, newState);
        player.displayClientMessage(Component.literal(newState.getMessage()).withStyle(ChatFormatting.DARK_GREEN), true);

        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide) return;

        var state = stack.getOrDefault(Registration.PATTERN_GENERATOR_COMPONENT, PatternGeneratorState.DEFAULT);
        if (state.pos1().isPresent()) {
            // if at least one position is defined, highlight the area
            BlockHighlightRenderer.highlightArea(level.getGameTime(), state.getRenderBounds());
        } else {
            BlockHighlightRenderer.clearAreaTask();
        }
    }

    private void resetPositions(ItemStack stack, Player player) {
        stack.set(Registration.PATTERN_GENERATOR_COMPONENT, PatternGeneratorState.DEFAULT);
        player.displayClientMessage(Component.literal("Positions reset.").withStyle(ChatFormatting.YELLOW), true);
    }

    private int generateAndPrintPattern(ClientLevel level, BlockPos altarPos, AABB bounds) {
        var entries = BlockPos.betweenClosedStream(bounds)
            .filter(pos -> !level.getBlockState(pos).isAir())
            .map(pos -> {
                var offset = pos.subtract(altarPos);
                var blockState = level.getBlockState(pos);
                var blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
                return new Entry(offset, blockId);
            })
            .toList();

        var sb = new StringBuilder();
        sb.append("\n.blockPattern(pattern =>").append("\n\tpattern");

        for (var entry : entries) {
            if (entry.offset.equals(BlockPos.ZERO)) continue;

            sb.append("\n\t\t.block(")
                .append("[")
                .append(entry.offset.getX())
                .append(", ")
                .append(entry.offset.getY())
                .append(", ")
                .append(entry.offset.getZ())
                .append("], ")
                .append("\"")
                .append(entry.id)
                .append("\"")
                .append(")");
        }

        sb.append("\n)");

        SummoningRituals.LOGGER.info(sb.toString());

        return entries.size() - 1;
    }

    private record Entry(BlockPos offset, ResourceLocation id) {}
}

package com.almostreliable.summoningrituals.util;

import com.almostreliable.summoningrituals.SummoningRitualsConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class GameUtils {

    private GameUtils() {}

    public static void sendPlayerMessage(
        @Nullable Player player, String translationKey, ChatFormatting color, Object... args
    ) {
        if (player == null) return;
        player.sendSystemMessage(
            Component.translatable(
                String.format("%s.%s.%s", "message", SummoningRitualsConstants.MOD_ID, translationKey),
                args
            ).withStyle(color)
        );
    }

    public static void dropItem(Level level, BlockPos pos, ItemStack stack, boolean offset) {
        ItemEntity.of(level, stack).spawn(
            level,
            pos.getX() + (offset ? 0.5 : 0),
            pos.getY() + (offset ? 0.5 : 0),
            pos.getZ() + (offset ? 0.5 : 0)
        );
    }

    public static void playSound(@Nullable Level level, BlockPos pos, SoundEvent sound) {
        if (level == null) return;
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.5f, 1f);
    }

    public static void renderCount(GuiGraphics guiGraphics, String text, int x, int y) {
        renderText(guiGraphics, text, ANCHOR.BOTTOM_RIGHT, x + 2, y + 2, 1, 0xFF_FFFF);
    }

    public static void renderText(
        GuiGraphics guiGraphics, String text, ANCHOR anchor, int x, int y, float scale, int color
    ) {
        PoseStack stack = guiGraphics.pose();

        stack.pushPose();
        {
            stack.translate(x, y, 200);
            stack.scale(scale, scale, 1);

            var xOffset = 0;
            var yOffset = 0;
            var font = Minecraft.getInstance().font;
            var width = font.width(text);
            var height = font.lineHeight;
            switch (anchor) {
                case TOP_LEFT:
                    // do nothing
                    break;
                case TOP_RIGHT:
                    xOffset -= width;
                    break;
                case BOTTOM_LEFT:
                    yOffset -= height;
                    break;
                case BOTTOM_RIGHT:
                    xOffset -= width;
                    yOffset -= height;
                    break;
            }

            guiGraphics.drawString(Minecraft.getInstance().font, text, xOffset, yOffset, color, true);
        }
        stack.popPose();
    }

    public enum ANCHOR {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
}

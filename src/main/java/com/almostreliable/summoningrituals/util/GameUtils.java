package com.almostreliable.summoningrituals.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;

public final class GameUtils {

    private GameUtils() {}

    public static RecipeManager getRecipeManager(@Nullable Level level) {
        if (level != null && level.getServer() != null) return level.getServer().getRecipeManager();
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return ServerLifecycleHooks.getCurrentServer().getRecipeManager();
        }
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getRecipeManager();
    }

    public static void dropItem(Level level, BlockPos pos, ItemStack stack, boolean offset) {
        spawnEntity(level, new ItemEntity(
            level,
            pos.getX() + (offset ? 0.5 : 0),
            pos.getY() + (offset ? 0.5 : 0),
            pos.getZ() + (offset ? 0.5 : 0),
            stack
        ));
    }

    public static void spawnEntity(Level level, Entity entity) {
        level.addFreshEntity(entity);
    }

    public static void renderCount(PoseStack stack, String text, int x, int y) {
        renderText(stack, text, ANCHOR.BOTTOM_RIGHT, x + 2, y + 2, 1, 0xFF_FFFF);
    }

    public static boolean isWithinBounds(double mX, double mY, int x, int y, int width, int height) {
        return mX >= x && mX <= x + width && mY >= y && mY <= y + height;
    }

    public static void renderText(PoseStack stack, String text, ANCHOR anchor, int x, int y, float scale, int color) {
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

            font.drawShadow(stack, text, xOffset, yOffset, color);
        }
        stack.popPose();
    }

    public enum ANCHOR {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
}

package com.almostreliable.summoningrituals.util;

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
}

package com.almostreliable.summoningrituals.recipe.output;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;

public interface RecipeOutput<T extends Entity> {

    BlockPos DEFAULT_OFFSET = new BlockPos(0, 2, 0);
    BlockPos DEFAULT_SPREAD = new BlockPos(1, 0, 1);
    Random RANDOM = new Random();

    default Vec3 getRandomPos(BlockPos origin) {
        var offset = offset().orElse(DEFAULT_OFFSET);
        var offsetVector = new Vec3(offset.getX(), offset.getY(), offset.getZ());

        var spread = spread().orElse(DEFAULT_SPREAD);
        var x = spread.getX() > 0 ? RANDOM.nextDouble(-spread.getX(), spread.getX()) / 2.0 : 0;
        var y = spread.getY() > 0 ? RANDOM.nextDouble(-spread.getY(), spread.getY()) / 2.0 : 0;
        var z = spread.getZ() > 0 ? RANDOM.nextDouble(-spread.getZ(), spread.getZ()) / 2.0 : 0;

        return Vec3.atCenterOf(origin).add(offsetVector).add(x, y, z);
    }

    Collection<T> spawn(ServerLevel level, BlockPos origin);

    Optional<BlockPos> spread();

    Optional<BlockPos> offset();
}

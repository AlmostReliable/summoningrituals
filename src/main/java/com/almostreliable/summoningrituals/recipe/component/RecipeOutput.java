package com.almostreliable.summoningrituals.recipe.component;

import com.almostreliable.summoningrituals.util.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Random;

public interface RecipeOutput {
    BlockPos DEFAULT_OFFSET = new BlockPos(0, 2, 0);
    BlockPos DEFAULT_SPREAD = new BlockPos(1, 0, 1);
    Random RANDOM = new Random();

    default Vec3 getRandomPos(BlockPos origin) {
        var spread = spread().orElse(DEFAULT_SPREAD);
        var offset = offset().orElse(DEFAULT_OFFSET);

        var x = spread.getX() > 0 ? RANDOM.nextDouble(-spread.getX(), spread.getX()) / 2.0 : 0;
        var y = spread.getY() > 0 ? RANDOM.nextDouble(-spread.getY(), spread.getY()) / 2.0 : 0;
        var z = spread.getZ() > 0 ? RANDOM.nextDouble(-spread.getZ(), spread.getZ()) / 2.0 : 0;
        return MathUtils.shiftToCenter(origin).add(MathUtils.vectorFromPos(offset)).add(x, y, z);
    }

    void spawn(ServerLevel level, BlockPos origin);

    Optional<BlockPos> spread();

    Optional<BlockPos> offset();
}

package com.almostreliable.summoningrituals.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class MathUtils {

    private MathUtils() {}

    public static Vec3 vectorFromBlockPos(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vec3 shiftToCenter(Vec3 pos) {
        return new Vec3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
    }

    public static int ensureDegree(Number degree) {
        return Math.abs(degree.intValue()) % 360;
    }
}

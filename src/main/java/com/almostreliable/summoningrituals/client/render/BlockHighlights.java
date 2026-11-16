package com.almostreliable.summoningrituals.client.render;

import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;

public final class BlockHighlights {

    private static final float LINE_WIDTH = 2f;
    private static final GizmoStyle WRONG_BLOCK_STYLE = GizmoStyle.stroke(0xFFFF_0000, LINE_WIDTH);
    private static final GizmoStyle AREA_STYLE = GizmoStyle.stroke(0xFF00_FFFF, LINE_WIDTH);
    private static final int HIGHLIGHT_DURATION_MILLIS = 5_000;

    private BlockHighlights() {}

    public static void highlightWrongPositions(Iterable<BlockPos> positions) {
        for (var pos : positions) {
            addWrongPosition(pos).persistForMillis(HIGHLIGHT_DURATION_MILLIS);
        }
    }

    public static void highlightWrongPosition(BlockPos pos) {
        addWrongPosition(pos);
    }

    public static void highlightArea(AABB area) {
        Gizmos.cuboid(area, AREA_STYLE).setAlwaysOnTop();
    }

    private static GizmoProperties addWrongPosition(BlockPos pos) {
        return Gizmos.cuboid(pos, WRONG_BLOCK_STYLE).setAlwaysOnTop();
    }
}

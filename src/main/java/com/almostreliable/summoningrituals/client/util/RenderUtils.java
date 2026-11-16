package com.almostreliable.summoningrituals.client.util;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

final class RenderUtils {

    // all directions plus null for the unculled quads
    private static final Direction[] QUAD_DIRECTIONS = Arrays.copyOf(Direction.values(), Direction.values().length + 1);

    private RenderUtils() {}

    static void forEachQuad(List<BlockStateModelPart> parts, Consumer<BakedQuad> action) {
        for (var part : parts) {
            for (var direction : QUAD_DIRECTIONS) {
                var quads = part.getQuads(direction);
                for (var quad : quads) {
                    action.accept(quad);
                }
            }
        }
    }
}

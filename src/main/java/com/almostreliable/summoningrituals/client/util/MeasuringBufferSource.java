package com.almostreliable.summoningrituals.client.util;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import com.mojang.blaze3d.vertex.VertexConsumer;

import org.jetbrains.annotations.Nullable;

/**
 * A {@link MultiBufferSource} that captures vertex positions without emitting any real geometry to
 * record min/max x/y/z values from all vertices submitted during a render pass.
 * <p>
 * It's used to uniformly scale entities to display them in GUIs. This method is much more reliable
 * than depending on an entity's bounding box since it doesn't always capture the full entity size.
 * <p>
 * idea by: embeddedt<br>
 * authored by: Almost Reliable, rlnt<br>
 * license: Unlicense
 * <p>
 * Check the README license section for more information.
 */
public class MeasuringBufferSource implements MultiBufferSource {

    private final MeasuringVertexConsumer instance = new MeasuringVertexConsumer();

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        // return the same consumer for all render layers, we only care about positions
        return instance;
    }

    @Nullable
    public MeasuringResult getData() {
        return instance.data;
    }

    public record MeasuringResult(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {

        public static final MeasuringResult EMPTY = new MeasuringResult(0, 0, 0, 0, 0, 0);

        MeasuringResult(float x, float y, float z) {
            this(x, y, z, x, y, z);
        }

        MeasuringResult measure(float x, float y, float z) {
            var mMinX = Math.min(minX, x);
            var mMinY = Math.min(minY, y);
            var mMinZ = Math.min(minZ, z);
            var mMaxX = Math.max(maxX, x);
            var mMaxY = Math.max(maxY, y);
            var mMaxZ = Math.max(maxZ, z);
            return new MeasuringResult(mMinX, mMinY, mMinZ, mMaxX, mMaxY, mMaxZ);
        }
    }

    private static final class MeasuringVertexConsumer implements VertexConsumer {

        @Nullable
        private MeasuringResult data;

        private void record(float x, float y, float z) {
            if (data == null) {
                data = new MeasuringResult(x, y, z);
            } else {
                data = data.measure(x, y, z);
            }
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            record(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setOverlay(int overlay) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float nx, float ny, float nz) {
            return this;
        }
    }
}

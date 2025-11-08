package com.almostreliable.summoningrituals.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * A {@link MultiBufferSource} that captures vertex positions without emitting any real geometry to
 * record min/max x/y/z values from all vertices submitted during a render pass.
 * <p>
 * It's used to uniformly scale entities to display them in GUIs. This method is much more reliable
 * than depending on an entity's bounding box since it doesn't always capture the full entity size.
 * <p>
 * idea by: embeddedt
 * authored by: Almost Reliable, rlnt
 * license: This is the only portion of the codebase that is not ARR. Feel free to use it in your mods
 * if you keep the credits.
 */
public class MeasuringBufferSource implements MultiBufferSource {

    private final MeasuringVertexConsumer instance = new MeasuringVertexConsumer();

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        // return the same consumer for all render layers, we only care about positions
        return instance;
    }

    public boolean hasData() {
        return instance.hasData();
    }

    public float diagonal() {
        float dx = instance.maxX - instance.minX;
        float dy = instance.maxY - instance.minY;
        float dz = instance.maxZ - instance.minZ;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float height() {
        return instance.maxY - instance.minY;
    }

    private static final class MeasuringVertexConsumer implements VertexConsumer {

        private float minX = Float.POSITIVE_INFINITY;
        private float minY = Float.POSITIVE_INFINITY;
        private float minZ = Float.POSITIVE_INFINITY;
        private float maxX = Float.NEGATIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;
        private float maxZ = Float.NEGATIVE_INFINITY;
        private boolean hasData = false;

        boolean hasData() {
            return hasData;
        }

        private void record(float x, float y, float z) {
            hasData = true;
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
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

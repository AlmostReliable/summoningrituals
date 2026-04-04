package com.almostreliable.summoningrituals.client.util;

import com.almostreliable.summoningrituals.ModConstants;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import java.util.Map;
import java.util.function.Consumer;

public final class AlphaBufferSource extends MultiBufferSource.BufferSource {

    public static final AlphaBufferSource INSTANCE = create();

    private final Map<RenderType, RenderType> delegatedRenderTypes = new Object2ObjectLinkedOpenHashMap<>();
    private float alpha = 1f;

    private static AlphaBufferSource create() {
        var alphaBufferSource = new AlphaBufferSource(new ByteBufferBuilder(256));

        for (var rt : RenderType.chunkBufferLayers()) {
            var delegated = new DelegateRenderType(
                rt,
                alphaBufferSource::onShaderSetup,
                alphaBufferSource::onShaderClear
            );

            alphaBufferSource.fixedBuffers.put(delegated, new ByteBufferBuilder(rt.bufferSize()));
            alphaBufferSource.delegatedRenderTypes.put(rt, delegated);
        }

        return alphaBufferSource;
    }

    private AlphaBufferSource(ByteBufferBuilder defaultBufferBuilder) {
        super(defaultBufferBuilder, new Object2ObjectLinkedOpenHashMap<>());
    }

    public void renderWithAlpha(float alpha, Consumer<AlphaBufferSource> consumer) {
        this.alpha = alpha;
        consumer.accept(this);
        endBatch();
        this.alpha = 1f;
    }

    private void onShaderSetup() {
        if (alpha != 1f) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1, 1, 1, alpha);
        }
    }

    private void onShaderClear() {
        if (alpha != 1f) {
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        if (renderType instanceof DelegateRenderType) {
            return super.getBuffer(renderType);
        }

        var delegated = delegatedRenderTypes.computeIfAbsent(
            renderType, rt -> {
                var d = new DelegateRenderType(rt, this::onShaderSetup, this::onShaderClear);
                fixedBuffers.put(d, new ByteBufferBuilder(rt.bufferSize()));
                return d;
            }
        );

        return super.getBuffer(delegated);
    }

    private static final class DelegateRenderType extends RenderType {

        private final RenderType delegate;

        private DelegateRenderType(RenderType delegate, Runnable setup, Runnable clear) {
            super(
                String.format("%s_delegate", ModConstants.MOD_ID),
                delegate.format,
                delegate.mode,
                delegate.bufferSize,
                delegate.affectsCrumbling,
                delegate.sortOnUpload,
                setup,
                clear
            );
            this.delegate = delegate;
        }

        @Override
        public void setupRenderState() {
            delegate.setupRenderState();
            super.setupRenderState();
        }

        @Override
        public void clearRenderState() {
            super.clearRenderState();
            delegate.clearRenderState();
        }

        @Override
        public String toString() {
            return "DelegateRenderType{delegate=" + delegate + "}";
        }
    }
}

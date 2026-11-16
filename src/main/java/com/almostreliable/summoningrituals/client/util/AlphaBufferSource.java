package com.almostreliable.summoningrituals.client.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Quaternionf;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class AlphaBufferSource implements SubmitNodeCollector {

    private final SubmitNodeCollector root;
    private final OrderedSubmitNodeCollector target;
    private final AlphaBufferSource owner;
    private final float alpha;
    private boolean submitted;

    public AlphaBufferSource(SubmitNodeCollector root, float alpha) {
        this.root = root;
        this.target = root;
        this.owner = this;
        this.alpha = alpha;
    }

    private AlphaBufferSource(AlphaBufferSource owner, OrderedSubmitNodeCollector target) {
        this.root = owner.root;
        this.target = target;
        this.owner = owner;
        this.alpha = owner.alpha;
    }

    public void resetSubmitted() {
        owner.submitted = false;
    }

    public boolean hasSubmitted() {
        return owner.submitted;
    }

    @Override
    public OrderedSubmitNodeCollector order(int order) {
        return new AlphaBufferSource(owner, root.order(order));
    }

    @Override
    public <S> void submitModel(
        Model<? super S> model,
        S state,
        PoseStack poseStack,
        RenderType renderType,
        int lightCoords,
        int overlayCoords,
        int tintedColor,
        @Nullable TextureAtlasSprite sprite,
        int outlineColor,
        ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        owner.submitted = true;
        var color = ARGB.multiplyAlpha(tintedColor, alpha);
        var translucentType = translucentType(renderType, sprite);
        if (translucentType == null) {
            target.submitModel(
                model,
                state,
                poseStack,
                renderType,
                lightCoords,
                overlayCoords,
                color,
                sprite,
                outlineColor,
                crumblingOverlay
            );
            return;
        }

        target.submitCustomGeometry(poseStack, translucentType, (pose, buffer) -> {
            var spriteBuffer = sprite == null ? buffer : sprite.wrap(buffer);
            model.setupAnim(state);
            model.renderToBuffer(poseStackOf(pose), spriteBuffer, lightCoords, overlayCoords, color);
        });
    }

    @Override
    public void submitModelPart(
        ModelPart modelPart,
        PoseStack poseStack,
        RenderType renderType,
        int lightCoords,
        int overlayCoords,
        @Nullable TextureAtlasSprite sprite,
        boolean sheeted,
        boolean hasFoil,
        int tintedColor,
        ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay,
        int outlineColor
    ) {
        owner.submitted = true;
        var color = ARGB.multiplyAlpha(tintedColor, alpha);
        var translucentType = translucentType(renderType, sprite);
        if (translucentType == null) {
            target.submitModelPart(
                modelPart,
                poseStack,
                renderType,
                lightCoords,
                overlayCoords,
                sprite,
                sheeted,
                hasFoil,
                color,
                crumblingOverlay,
                outlineColor
            );
            return;
        }

        target.submitCustomGeometry(poseStack, translucentType, (pose, buffer) -> {
            var spriteBuffer = sprite == null ? buffer : sprite.wrap(buffer);
            modelPart.render(poseStackOf(pose), spriteBuffer, lightCoords, overlayCoords, color);
        });
    }

    @Override
    public void submitBlockModel(
        PoseStack poseStack,
        RenderType renderType,
        List<BlockStateModelPart> parts,
        int[] tintLayers,
        int lightCoords,
        int overlayCoords,
        int outlineColor
    ) {
        submitBlockQuads(poseStack, parts, tintLayers, lightCoords, overlayCoords);
    }

    @Override
    public void submitMultiLayerBlockModel(
        PoseStack poseStack,
        List<BlockStateModelPart> modelParts,
        boolean translucent,
        int[] tintLayers,
        int lightCoords,
        int overlayCoords,
        int outlineColor
    ) {
        submitBlockQuads(poseStack, modelParts, tintLayers, lightCoords, overlayCoords);
    }

    @Override
    public void submitItem(
        PoseStack poseStack,
        ItemDisplayContext displayContext,
        int lightCoords,
        int overlayCoords,
        int outlineColor,
        int[] tintLayers,
        List<BakedQuad> quads,
        ItemStackRenderState.FoilType foilType
    ) {
        if (quads.isEmpty()) return;

        owner.submitted = true;

        var quadsByType = new LinkedHashMap<RenderType, List<BakedQuad>>();

        for (var quad : quads) {
            var renderType = quad.materialInfo().itemRenderType();
            var translucentType = translucentType(renderType, null);
            var quadType = translucentType == null ? renderType : translucentType;
            quadsByType.computeIfAbsent(quadType, _ -> new ArrayList<>()).add(quad);
        }

        quadsByType.forEach((renderType, typeQuads) -> target.submitCustomGeometry(
            poseStack,
            renderType,
            (pose, buffer) -> putQuads(pose, buffer, typeQuads, tintLayers, lightCoords, overlayCoords)
        ));
    }

    @Override
    public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, CustomGeometryRenderer customGeometryRenderer) {
        owner.submitted = true;
        target.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            var alphaBuffer = new AlphaVertexConsumer(buffer, alpha);
            customGeometryRenderer.render(pose, alphaBuffer);
        });
    }

    @Override
    public void submitText(
        PoseStack poseStack,
        float x,
        float y,
        FormattedCharSequence string,
        boolean dropShadow,
        Font.DisplayMode displayMode,
        int lightCoords,
        int color,
        int backgroundColor,
        int outlineColor
    ) {
        owner.submitted = true;
        target.submitText(
            poseStack,
            x,
            y,
            string,
            dropShadow,
            displayMode,
            lightCoords,
            ARGB.multiplyAlpha(color, alpha),
            ARGB.multiplyAlpha(backgroundColor, alpha),
            ARGB.multiplyAlpha(outlineColor, alpha)
        );
    }

    @Override
    public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState) {
        owner.submitted = true;
        target.submitMovingBlock(poseStack, movingBlockRenderState);
    }

    @Override
    public void submitShadow(PoseStack poseStack, float radius, List<EntityRenderState.ShadowPiece> pieces) {
        target.submitShadow(poseStack, radius, pieces);
    }

    @Override
    public void submitNameTag(
        PoseStack poseStack,
        @Nullable Vec3 nameTagAttachment,
        int offset,
        Component name,
        boolean seeThrough,
        int lightCoords,
        double distanceToCameraSq,
        CameraRenderState camera
    ) {
        target.submitNameTag(poseStack, nameTagAttachment, offset, name, seeThrough, lightCoords, distanceToCameraSq, camera);
    }

    @Override
    public void submitFlame(PoseStack poseStack, EntityRenderState renderState, Quaternionf rotation) {
        target.submitFlame(poseStack, renderState, rotation);
    }

    @Override
    public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
        target.submitLeash(poseStack, leashState);
    }

    @Override
    public void submitBreakingBlockModel(PoseStack poseStack, BlockStateModel model, long seed, int progress) {
        target.submitBreakingBlockModel(poseStack, model, seed, progress);
    }

    @Override
    public void submitParticleGroup(ParticleGroupRenderer particleGroupRenderer) {
        target.submitParticleGroup(particleGroupRenderer);
    }

    @SuppressWarnings("deprecation")
    private void submitBlockQuads(
        PoseStack poseStack,
        List<BlockStateModelPart> parts,
        int[] tintLayers,
        int lightCoords,
        int overlayCoords
    ) {
        var quads = new ArrayList<BakedQuad>();
        BlockModelQuads.forEach(parts, quads::add);

        if (quads.isEmpty()) return;

        owner.submitted = true;

        var renderType = RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS, false);
        target.submitCustomGeometry(
            poseStack,
            renderType,
            (pose, buffer) -> putQuads(pose, buffer, quads, tintLayers, lightCoords, overlayCoords)
        );
    }

    private void putQuads(
        PoseStack.Pose pose,
        VertexConsumer buffer,
        List<BakedQuad> quads,
        int[] tintLayers,
        int lightCoords,
        int overlayCoords
    ) {
        var quadInstance = new QuadInstance();
        quadInstance.setLightCoords(lightCoords);
        quadInstance.setOverlayCoords(overlayCoords);

        for (var quad : quads) {
            var tintIndex = quad.materialInfo().tintIndex();
            var color = tintIndex >= 0 && tintIndex < tintLayers.length ? tintLayers[tintIndex] : -1;
            quadInstance.setColor(ARGB.multiplyAlpha(color, alpha));
            buffer.putBakedQuad(pose, quad, quadInstance);
        }
    }

    @Nullable
    private static RenderType translucentType(RenderType renderType, @Nullable TextureAtlasSprite sprite) {
        var texture = sprite == null ? textureOf(renderType) : sprite.atlasLocation();
        return texture == null ? null : RenderTypes.entityTranslucent(texture, false);
    }

    @Nullable
    private static Identifier textureOf(RenderType renderType) {
        var textures = renderType.state.textures;
        var binding = textures.get("Sampler0");
        if (binding == null) {
            binding = textures.values().stream().findFirst().orElse(null);
        }

        return binding == null ? null : binding.location();
    }

    private static PoseStack poseStackOf(PoseStack.Pose pose) {
        var poseStack = new PoseStack();
        poseStack.last().set(pose);

        return poseStack;
    }

    private static final class AlphaVertexConsumer implements VertexConsumer {

        private final VertexConsumer delegate;
        private final float alpha;

        private AlphaVertexConsumer(VertexConsumer delegate, float alpha) {
            this.delegate = delegate;
            this.alpha = alpha;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            delegate.setColor(r, g, b, (int) (a * alpha));
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            delegate.setColor(ARGB.multiplyAlpha(color, alpha));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            delegate.setLineWidth(width);
            return this;
        }
    }
}

package com.almostreliable.summoningrituals.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Quaternionf;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A {@link SubmitNodeCollector} that captures vertex positions without emitting any real geometry to
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
public class MeasuringBufferSource implements SubmitNodeCollector, MultiBufferSource {

    private final MeasuringVertexConsumer instance = new MeasuringVertexConsumer();
    private final QuadInstance quadInstance = new QuadInstance();

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        // return the same consumer for all render layers, we only care about positions
        return instance;
    }

    @Override
    public OrderedSubmitNodeCollector order(int order) {
        return this;
    }

    @Override
    public <S> void submitModel(
        Model<? super S> model, S state, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords,
        int tintedColor, @Nullable TextureAtlasSprite sprite, int outlineColor,
        ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        model.setupAnim(state);
        model.renderToBuffer(poseStack, instance, lightCoords, overlayCoords, tintedColor);
    }

    @Override
    public void submitModelPart(
        ModelPart modelPart, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords,
        @Nullable TextureAtlasSprite sprite, boolean sheeted, boolean hasFoil, int tintedColor,
        ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, int outlineColor
    ) {
        modelPart.render(poseStack, instance, lightCoords, overlayCoords, tintedColor);
    }

    @Override
    public void submitBlockModel(
        PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> parts, int[] tintLayers, int lightCoords, int overlayCoords,
        int outlineColor
    ) {
        measureBlockParts(poseStack, parts);
    }

    @Override
    public void submitMultiLayerBlockModel(
        PoseStack poseStack, List<BlockStateModelPart> modelParts, boolean translucent, int[] tintLayers, int lightCoords, int overlayCoords,
        int outlineColor
    ) {
        measureBlockParts(poseStack, modelParts);
    }

    @Override
    public void submitItem(
        PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers,
        List<BakedQuad> quads, ItemStackRenderState.FoilType foilType
    ) {
        for (var quad : quads) {
            instance.putBakedQuad(poseStack.last(), quad, quadInstance);
        }
    }

    @Override
    public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, CustomGeometryRenderer customGeometryRenderer) {
        customGeometryRenderer.render(poseStack.last(), instance);
    }

    @Override
    public void submitText(
        PoseStack poseStack, float x, float y, FormattedCharSequence string, boolean dropShadow, Font.DisplayMode displayMode, int lightCoords,
        int color, int backgroundColor, int outlineColor
    ) {
        Minecraft.getInstance().font.drawInBatch(
            string, x, y, color, dropShadow, poseStack.last().pose(), this, displayMode, backgroundColor, lightCoords
        );
    }

    @Override
    public void submitNameTag(
        PoseStack poseStack, @Nullable Vec3 nameTagAttachment, int offset, Component name, boolean seeThrough, int lightCoords,
        double distanceToCameraSq, CameraRenderState camera
    ) {
        if (nameTagAttachment == null) return;

        var font = Minecraft.getInstance().font;
        poseStack.pushPose();
        poseStack.translate(nameTagAttachment.x, nameTagAttachment.y + 0.5, nameTagAttachment.z);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(0.025f, -0.025f, 0.025f);
        font.drawInBatch(
            name, -font.width(name) / 2f, offset, -1, false, poseStack.last().pose(), this, Font.DisplayMode.NORMAL, 0, lightCoords
        );
        poseStack.popPose();
    }

    @Override
    public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState) {}

    @Override
    public void submitShadow(PoseStack poseStack, float radius, List<EntityRenderState.ShadowPiece> pieces) {}

    @Override
    public void submitFlame(PoseStack poseStack, EntityRenderState renderState, Quaternionf rotation) {}

    @Override
    public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {}

    @Override
    public void submitBreakingBlockModel(PoseStack poseStack, BlockStateModel model, long seed, int progress) {}

    @Override
    public void submitParticleGroup(ParticleGroupRenderer particleGroupRenderer) {}

    private void measureBlockParts(PoseStack poseStack, List<BlockStateModelPart> parts) {
        var pose = poseStack.last();
        BlockModelQuads.forEach(parts, quad -> instance.putBakedQuad(pose, quad, quadInstance));
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
        public VertexConsumer setColor(int color) {
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

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }
    }
}

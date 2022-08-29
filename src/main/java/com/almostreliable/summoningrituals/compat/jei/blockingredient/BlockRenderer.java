package com.almostreliable.summoningrituals.compat.jei.blockingredient;

import com.almostreliable.summoningrituals.compat.jei.AltarRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;

public class BlockRenderer implements IIngredientRenderer<BlockState> {

    private final Minecraft mc;
    private final BlockRenderDispatcher blockRenderer;
    private final int size;

    public BlockRenderer(int size) {
        mc = Minecraft.getInstance();
        blockRenderer = mc.getBlockRenderer();
        this.size = size;
    }

    @Override
    public void render(PoseStack stack, BlockState blockState) {
        stack.pushPose();
        {
            stack.translate(size * 4f / 5f, size * 4f / 5f, 0);
            stack.mulPose(Vector3f.ZN.rotationDegrees(180));
            stack.scale(size * 3f / 5f, size * 3f / 5f, 1);
            stack.mulPose(Vector3f.XN.rotationDegrees(30));
            stack.mulPose(Vector3f.YP.rotationDegrees(45));
            var bufferSource = mc.renderBuffers().bufferSource();
            blockRenderer.renderSingleBlock(
                blockState,
                stack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                EmptyModelData.INSTANCE
            );
            bufferSource.endBatch();
        }
        stack.popPose();
    }

    @Override
    public List<Component> getTooltip(BlockState blockState, TooltipFlag tooltipFlag) {
        return List.of(new TextComponent("todo, some block lol"));
    }

    @Override
    public int getWidth() {
        return size;
    }

    @Override
    public int getHeight() {
        return size;
    }
}

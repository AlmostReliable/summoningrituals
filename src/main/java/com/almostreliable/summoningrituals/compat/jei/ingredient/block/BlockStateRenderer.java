package com.almostreliable.summoningrituals.compat.jei.ingredient.block;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.util.TextUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockStateRenderer implements IIngredientRenderer<BlockState> {

    private final Minecraft mc;
    private final BlockRenderDispatcher blockRenderer;
    private final Map<Integer, List<Component>> tooltipCache;
    private final int size;

    public BlockStateRenderer(int size) {
        mc = Minecraft.getInstance();
        blockRenderer = mc.getBlockRenderer();
        tooltipCache = new HashMap<>();
        this.size = size;
    }

    @Override
    public void render(PoseStack stack, BlockState blockState) {
        stack.pushPose();
        {
            stack.translate(0.93f * size, 0.77f * size, 0);
            stack.scale(0.625f * size, 0.625f * size, 0.625f * size);
            stack.mulPose(Vector3f.ZN.rotationDegrees(180));
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
        var stack = new ItemStack(blockState.getBlock());
        try {
            var stateId = Block.getId(blockState);
            var tooltip = tooltipCache.get(stateId);
            if (tooltip != null) return tooltip;

            tooltip = stack.getTooltipLines(mc.player, tooltipFlag);
            tooltip.set(
                0,
                TextUtils.translate(Constants.TOOLTIP, Constants.BLOCK_BELOW, ChatFormatting.GOLD)
                    .append(": ")
                    .append(TextUtils.colorize(tooltip.get(0).getString(), ChatFormatting.WHITE))
            );
            constructTooltip(blockState, tooltip);
            tooltipCache.put(stateId, tooltip);
            return tooltip;
        } catch (Exception e) {
            return List.of(new TextComponent("Error rendering tooltip!").append(e.getMessage())
                .withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public int getWidth() {
        return size;
    }

    @Override
    public int getHeight() {
        return size;
    }

    private void constructTooltip(BlockState blockState, List<Component> tooltip) {
        var defaultState = blockState.getBlock().defaultBlockState();
        List<String> modifiedProps = new ArrayList<>();
        for (var property : blockState.getProperties()) {
            if (!blockState.getValue(property).equals(defaultState.getValue(property))) {
                modifiedProps.add(property.getName() + ": " + blockState.getValue(property));
            }
        }
        if (modifiedProps.isEmpty()) return;

        tooltip.add(TextUtils.translate(Constants.TOOLTIP, Constants.PROPERTIES, ChatFormatting.AQUA)
            .append(TextUtils.colorize(":", ChatFormatting.AQUA)));
        for (var prop : modifiedProps) {
            tooltip.add(TextUtils.colorize("» ", ChatFormatting.GRAY)
                .append(TextUtils.colorize(prop, ChatFormatting.WHITE)));
        }
    }
}

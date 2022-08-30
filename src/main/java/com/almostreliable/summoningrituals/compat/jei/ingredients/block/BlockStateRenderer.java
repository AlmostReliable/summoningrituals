package com.almostreliable.summoningrituals.compat.jei.ingredients.block;

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
import net.minecraft.world.entity.player.Player;
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
    private final Player player;
    private final Map<Integer, List<Component>> propertyTooltipCache;
    private final int size;

    public BlockStateRenderer(int size) {
        mc = Minecraft.getInstance();
        blockRenderer = mc.getBlockRenderer();
        this.player = mc.player;
        propertyTooltipCache = new HashMap<>();
        this.size = size;
    }

    @Override
    public void render(PoseStack stack, BlockState blockState) {
        stack.pushPose();
        {
            stack.translate(0.95f * size, 0.75f * size, 0);
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
            var tooltip = stack.getTooltipLines(player, tooltipFlag);

            var stateId = Block.getId(blockState);
            if (propertyTooltipCache.containsKey(stateId)) {
                return propertyTooltipCache.get(stateId);
            }

            cacheTooltip(blockState, tooltip, stateId);
            return tooltip;
        } catch (RuntimeException | LinkageError e) {
            return List.of(new TextComponent("Error rendering tooltip!").append(e.getMessage())
                .withStyle(ChatFormatting.DARK_RED));
        }
    }

    private void cacheTooltip(BlockState blockState, List<Component> tooltip, int stateId) {
        var defaultState = blockState.getBlock().defaultBlockState();
        List<String> modifiedProps = new ArrayList<>();
        for (var property : blockState.getProperties()) {
            if (!blockState.getValue(property).equals(defaultState.getValue(property))) {
                modifiedProps.add(property.getName() + ": " + blockState.getValue(property));
            }
        }

        if (modifiedProps.isEmpty()) {
            propertyTooltipCache.put(stateId, null);
        } else {
            tooltip.add(TextComponent.EMPTY);
            tooltip.add(TextUtils.translate("tooltip", "relevant_properties", ChatFormatting.AQUA)
                .append(TextUtils.colorize(":", ChatFormatting.AQUA)));
            for (var prop : modifiedProps) {
                tooltip.add(TextUtils.colorize("» ", ChatFormatting.GRAY).append(TextUtils.colorize(prop, ChatFormatting.WHITE)));
            }
            propertyTooltipCache.put(stateId, tooltip);
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
}

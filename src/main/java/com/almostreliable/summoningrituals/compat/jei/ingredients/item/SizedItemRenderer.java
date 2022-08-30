package com.almostreliable.summoningrituals.compat.jei.ingredients.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SizedItemRenderer implements IIngredientRenderer<ItemStack> {

    private final Font font;
    private final ItemRenderer itemRenderer;
    private final Player player;
    private final int size;

    public SizedItemRenderer(int size) {
        var mc = Minecraft.getInstance();
        this.font = mc.font;
        this.itemRenderer = mc.getItemRenderer();
        this.player = mc.player;
        this.size = size;
    }

    @Override
    public void render(PoseStack stack, ItemStack altar) {
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        {
            modelViewStack.mulPoseMatrix(stack.last().pose());
            var scale = size / 16f;
            modelViewStack.scale(scale, scale, scale);
            RenderSystem.enableDepthTest();
            itemRenderer.renderAndDecorateFakeItem(altar, 0, 0);
            itemRenderer.renderGuiItemDecorations(font, altar, 0, 0);
            RenderSystem.disableBlend();
        }
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public List<Component> getTooltip(ItemStack altar, TooltipFlag tooltipFlag) {
        try {
            return altar.getTooltipLines(player, tooltipFlag);
        } catch (RuntimeException | LinkageError e) {
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
}

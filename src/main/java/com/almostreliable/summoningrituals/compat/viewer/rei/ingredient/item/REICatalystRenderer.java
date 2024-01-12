package com.almostreliable.summoningrituals.compat.viewer.rei.ingredient.item;

import com.almostreliable.summoningrituals.compat.viewer.common.CatalystRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class REICatalystRenderer extends CatalystRenderer implements EntryRenderer<ItemStack> {

    public REICatalystRenderer(int size) {
        super(size);
    }

    @Override
    public void render(
        EntryStack<ItemStack> entry, GuiGraphics guiGraphics, Rectangle bounds, int mX, int mY, float delta
    ) {
        PoseStack stack = guiGraphics.pose();
        stack.pushPose();
        stack.translate(bounds.x, bounds.y, 0);
        render(guiGraphics, entry.getValue());
        stack.popPose();
    }

    @Nullable
    @Override
    public Tooltip getTooltip(EntryStack<ItemStack> entry, TooltipContext context) {
        return Tooltip.create(context.getPoint(), getTooltip(entry.getValue(), context.getFlag()));
    }
}

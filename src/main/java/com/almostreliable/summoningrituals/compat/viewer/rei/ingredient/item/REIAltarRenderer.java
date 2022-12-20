package com.almostreliable.summoningrituals.compat.viewer.rei.ingredient.item;

import com.almostreliable.summoningrituals.compat.viewer.common.AltarRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class REIAltarRenderer extends AltarRenderer implements EntryRenderer<ItemStack> {

    public REIAltarRenderer(int size) {
        super(size);
    }

    @Override
    public void render(
        EntryStack<ItemStack> entry, PoseStack stack, Rectangle bounds, int mX, int mY, float delta
    ) {
        stack.pushPose();
        stack.translate(bounds.x - 1, bounds.y - 1, 0);
        render(stack, null);
        stack.popPose();
    }

    @Nullable
    @Override
    public Tooltip getTooltip(EntryStack<ItemStack> entry, TooltipContext context) {
        return Tooltip.create(context.point, getTooltip(ItemStack.EMPTY, context.flag));
    }
}

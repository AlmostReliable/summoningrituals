package com.almostreliable.summoningrituals.compat.viewer.rei.ingredient.block;

import com.almostreliable.summoningrituals.compat.viewer.common.BlockReferenceRenderer;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;

import javax.annotation.Nullable;

public class REIBlockReferenceRenderer extends BlockReferenceRenderer implements EntryRenderer<BlockReference> {

    REIBlockReferenceRenderer(int size) {
        super(size);
    }

    @Override
    public void render(
        EntryStack<BlockReference> entry, PoseStack stack, Rectangle bounds, int mX, int mY, float delta
    ) {
        stack.pushPose();
        stack.translate(bounds.x - 1, bounds.y - 1, 0);
        render(stack, entry.value);
        stack.popPose();
    }

    @Nullable
    @Override
    public Tooltip getTooltip(EntryStack<BlockReference> entry, TooltipContext context) {
        return Tooltip.create(context.point, getTooltip(entry.value, context.flag));
    }
}

package com.almostreliable.summoningrituals.compat.viewer.rei.ingredient.mob;

import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.common.MobRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;

public class REIMobRenderer extends MobRenderer implements EntryRenderer<EntityIngredient> {

    REIMobRenderer(int size) {
        super(size);
    }

    @Override
    public void render(
        EntryStack<EntityIngredient> entry, GuiGraphics guiGraphics, Rectangle bounds, int mX, int mY, float delta
    ) {
        PoseStack stack = guiGraphics.pose();
        stack.pushPose();
        stack.translate(bounds.x - 1, bounds.y - 1, 0);
        render(guiGraphics, entry.getValue());
        stack.popPose();
    }

    @Nullable
    @Override
    public Tooltip getTooltip(EntryStack<EntityIngredient> entry, TooltipContext context) {
        return Tooltip.create(context.getPoint(), getTooltip(entry.getValue(), context.getFlag()));
    }
}

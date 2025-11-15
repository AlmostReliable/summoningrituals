package com.almostreliable.summoningrituals.compat.viewer.emi.widget;

import net.minecraft.client.gui.GuiGraphics;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;

/**
 * Custom {@link SlotWidget} implementation that uses 16x16 instead of 18x18 because
 * this doesn't render slot borders. This is used to prevent the slot taking up
 * more space than it needs.
 */
public class InvisibleSlotWidget extends SlotWidget {

    public static final int SLOT_SIZE = 16;

    public InvisibleSlotWidget(EmiIngredient stack, int x, int y) {
        super(stack, x, y);
        this.drawBack = false;
        this.bounds = new Bounds(x, y, SLOT_SIZE, SLOT_SIZE);
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void drawSlotHighlight(GuiGraphics draw, Bounds bounds) {
        var newBounds = new Bounds(
            bounds.x() - 1,
            bounds.y() - 1,
            bounds.width() + 2,
            bounds.height() + 2
        );

        super.drawSlotHighlight(draw, newBounds);
    }
}

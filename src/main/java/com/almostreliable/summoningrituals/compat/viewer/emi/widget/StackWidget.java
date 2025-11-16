package com.almostreliable.summoningrituals.compat.viewer.emi.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom {@link SlotWidget} implementation used for rendering a stack icon
 * and a custom tooltip without functionality for clicking or hovering.
 */
public class StackWidget extends InvisibleSlotWidget {

    public StackWidget(EmiIngredient stack, int x, int y) {
        super(stack, x, y);
    }

    @Override
    public void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        // do nothing
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        var list = new ArrayList<ClientTooltipComponent>();
        for (var supplier : tooltipSuppliers) {
            list.add(supplier.get());
        }
        return list;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}

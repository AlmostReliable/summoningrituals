package com.almostreliable.summoningrituals.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PatternPreviewTooltipComponent implements ClientTooltipComponent {

    private static final int CELL_SIZE = 18;
    private static final int PADDING = 2;
    private static final int COLUMNS = 8;

    private final List<ItemStack> stacks;

    public PatternPreviewTooltipComponent(Data component) {
        this.stacks = component.stacks();
    }

    @Override
    public int getHeight() {
        var rows = Math.max(1, (int) Math.ceil(stacks.size() / (double) COLUMNS));
        return rows * CELL_SIZE + PADDING * 2;
    }

    @Override
    public int getWidth(Font font) {
        var columns = Math.min(COLUMNS, Math.max(1, stacks.size()));
        return columns * CELL_SIZE + PADDING * 2;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        for (var i = 0; i < stacks.size(); i++) {
            var col = i % COLUMNS;
            var row = i / COLUMNS;

            var drawX = x + PADDING + col * CELL_SIZE + 1;
            var drawY = y + PADDING + row * CELL_SIZE + 1;

            var stack = stacks.get(i);
            guiGraphics.renderItem(stack, drawX, drawY);
            guiGraphics.renderItemDecorations(font, stack, drawX, drawY);
        }
    }

    public record Data(List<ItemStack> stacks) implements TooltipComponent {}
}

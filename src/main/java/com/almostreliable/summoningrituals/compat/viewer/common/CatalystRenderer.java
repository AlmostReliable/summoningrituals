package com.almostreliable.summoningrituals.compat.viewer.common;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class CatalystRenderer extends SizedItemRenderer {

    public CatalystRenderer(int size) {
        super(size);
    }

    @Override
    public List<Component> getTooltip(ItemStack stack, TooltipFlag tooltipFlag) {
        try {
            var tooltip = stack.getTooltipLines(mc.player, tooltipFlag);
            tooltip.set(
                0,
                TextUtils.translate(Constants.TOOLTIP, Constants.CATALYST, ChatFormatting.GOLD)
                    .append(": ")
                    .append(TextUtils.colorize(tooltip.get(0).getString(), ChatFormatting.WHITE))
            );
            tooltip.add(TextUtils.translate(Constants.TOOLTIP, f("{}_desc", Constants.CATALYST), ChatFormatting.GRAY));
            return tooltip;
        } catch (RuntimeException | LinkageError e) {
            return List.of(Component.literal("Error rendering tooltip!").append(e.getMessage())
                .withStyle(ChatFormatting.DARK_RED));
        }
    }
}

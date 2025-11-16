package com.almostreliable.summoningrituals.compat.viewer.emi.widget;

import com.almostreliable.summoningrituals.data.SummoningLang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiIngredient;

import java.util.ArrayList;
import java.util.List;

public class InitiatorSlotWidget extends InvisibleSlotWidget {

    public InitiatorSlotWidget(EmiIngredient stack, int x, int y) {
        super(stack, x, y);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        var list = new ArrayList<ClientTooltipComponent>();
        if (getStack().isEmpty()) return list;

        var tooltipLines = getStack().getTooltip();
        if (tooltipLines.isEmpty()) return list;

        if (!(tooltipLines.getFirst() instanceof ClientTextTooltip stackTooltip)) {
            return list;
        }

        var itemName = Component.empty();
        stackTooltip.text.accept((posInCurrentSequence, style, codePoint) -> {
            var s = String.valueOf(Character.toChars(codePoint));
            var sComponent = Component.literal(s).setStyle(style);
            itemName.append(sComponent);
            return true;
        });

        var initiatorComponent = SummoningLang.INITIATOR.get()
            .append(": ")
            .withStyle(ChatFormatting.GOLD)
            .append(itemName.withStyle(ChatFormatting.WHITE));

        list.add(EmiTooltipComponents.of(initiatorComponent));
        list.add(EmiTooltipComponents.of(SummoningLang.INSERT_LAST.get().withStyle(ChatFormatting.GRAY)));
        tooltipLines.removeFirst();
        list.addAll(tooltipLines);
        addSlotTooltip(list);
        return list;
    }
}

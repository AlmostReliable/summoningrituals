package com.almostreliable.summoningrituals.compat.viewer.jei.entity;

import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredientRenderer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

import mezz.jei.api.ingredients.IIngredientRenderer;

import org.jspecify.annotations.Nullable;

import java.util.List;

public record EntityIngredientJeiRenderer(boolean scissor, boolean count) implements IIngredientRenderer<EntityIngredient> {

    public static final EntityIngredientJeiRenderer BOOKMARK_RENDERER = new EntityIngredientJeiRenderer(true, false);
    public static final EntityIngredientJeiRenderer INPUT_RENDERER = new EntityIngredientJeiRenderer(false, true);
    public static final EntityIngredientJeiRenderer OUTPUT_RENDERER = new EntityIngredientJeiRenderer(true, true);

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, EntityIngredient entityIngredient) {
        EntityIngredientRenderer.render(guiGraphics, entityIngredient, scissor, count);
    }

    @Override
    public List<Component> getTooltip(
        EntityIngredient entityIngredient,
        Item.TooltipContext tooltipContext,
        @Nullable Player player,
        TooltipFlag tooltipFlag
    ) {
        return entityIngredient.getTooltip(count, tooltipFlag.isAdvanced());
    }

    // still abstract in JEI, delete together with the deprecated overload
    @SuppressWarnings("removal")
    @Override
    public List<Component> getTooltip(EntityIngredient entityIngredient, TooltipFlag tooltipFlag) {
        return entityIngredient.getTooltip(count, tooltipFlag.isAdvanced());
    }
}

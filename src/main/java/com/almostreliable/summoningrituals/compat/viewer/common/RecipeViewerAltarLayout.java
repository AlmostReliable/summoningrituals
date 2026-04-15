package com.almostreliable.summoningrituals.compat.viewer.common;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.client.render.BlockPatternRenderer;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.condition.pattern.BlockPatternCondition;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class RecipeViewerAltarLayout {

    public static final int SLOT_SIZE = 16;
    protected static final ResourceLocation TEXTURE = SummoningRituals.getRL(String.format("textures/gui/%s.png", Constants.RECIPE_VIEWER));
    protected static final int TEXTURE_WIDTH = 172;
    public static final int TEXTURE_HEIGHT = 148;
    protected static final int CENTER_X = TEXTURE_WIDTH / 2;
    protected static final int CENTER_Y = TEXTURE_HEIGHT / 2;
    private static final int INPUT_RADIUS = 46;

    public int getWidth() {
        return TEXTURE_WIDTH;
    }

    public int getHeight() {
        return TEXTURE_HEIGHT;
    }

    public void onPreviewButtonClicked(Optional<BlockPatternCondition> blockPattern) {
        if (blockPattern.isEmpty()) return;
        BlockPatternRenderer.scheduleTask(blockPattern.get());
    }

    protected void createInitiatorSlot(SlotConsumer slotConsumer) {
        var x = CENTER_X - SLOT_SIZE / 2;
        var y = CENTER_Y - SLOT_SIZE / 2 - 34;
        slotConsumer.accept(x, y, 0);
    }

    protected void createInputSlots(AltarRecipe recipe, SlotConsumer slotConsumer) {
        var inputs = recipe.inputs();
        var itemInputs = inputs.itemInputs();
        var entityInputs = inputs.entityInputs();
        var fakeEntityInputs = inputs.fakeEntityInputs();
        var inputSlots = itemInputs.size() + entityInputs.size() + fakeEntityInputs.size();

        for (var i = 0; i < inputSlots; i++) {
            var x = CENTER_X + (int) (Math.cos(i * 2 * Math.PI / inputSlots) * INPUT_RADIUS) - (SLOT_SIZE / 2);
            var y = CENTER_Y - 10 + (int) (Math.sin(i * 2 * Math.PI / inputSlots) * INPUT_RADIUS) - (SLOT_SIZE / 2);
            slotConsumer.accept(x, y, i);
        }
    }

    protected void createOutputSlots(AltarRecipe recipe, SlotConsumer slotConsumer) {
        var outputs = recipe.outputs();
        var itemOutputs = outputs.itemOutputs();
        var entityOutputs = outputs.entityOutputs();
        var displayOutputs = outputs.displayOutputs();
        var outputSlots = itemOutputs.size() + entityOutputs.size() + displayOutputs.size();

        for (var i = 0; i < outputSlots; i++) {
            var x = 2 + i * SLOT_SIZE;
            var y = TEXTURE_HEIGHT - SLOT_SIZE - 2;
            slotConsumer.accept(x, y, i);
        }
    }

    @FunctionalInterface
    public interface SlotConsumer {

        void accept(int x, int y, int slot);
    }
}

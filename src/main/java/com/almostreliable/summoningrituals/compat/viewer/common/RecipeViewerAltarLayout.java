package com.almostreliable.summoningrituals.compat.viewer.common;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.client.render.PatternPreviewRenderer;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.condition.custom.BlockPatternCheck;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipeViewerAltarLayout {

    public static final int SLOT_SIZE = 16;
    protected static final ResourceLocation TEXTURE = SummoningRituals.getRL(String.format("textures/gui/%s.png", Constants.RECIPE_VIEWER));
    protected static final int TEXTURE_WIDTH = 172;
    public static final int TEXTURE_HEIGHT = 148;
    protected static final int CENTER_X = TEXTURE_WIDTH / 2;
    protected static final int CENTER_Y = TEXTURE_HEIGHT / 2;
    private static final int INPUT_RADIUS = 46;

    @Nullable
    protected CachedBlockPattern cachedBlockPattern;

    public int getWidth() {
        return TEXTURE_WIDTH;
    }

    public int getHeight() {
        return TEXTURE_HEIGHT;
    }

    protected void cacheBlockPatternCondition(ResourceLocation recipeId, List<LootItemCondition> recipeConditions) {
        var found = false;
        for (var recipeCondition : recipeConditions) {
            if (!(recipeCondition instanceof BlockPatternCheck patternCheck)) {
                continue;
            }

            cachedBlockPattern = new CachedBlockPattern(recipeId, patternCheck);
            found = true;
            break;
        }

        if (!found) {
            cachedBlockPattern = CachedBlockPattern.NONE;
        }
    }

    public void onPreviewButtonClicked() {
        if (cachedBlockPattern == null || cachedBlockPattern == CachedBlockPattern.NONE) return;
        PatternPreviewRenderer.scheduleTask(cachedBlockPattern.blockPattern);
    }

    protected void createInitiatorSlot(SlotConsumer slotConsumer) {
        var x = CENTER_X - SLOT_SIZE / 2;
        var y = CENTER_Y - SLOT_SIZE / 2 - 34;
        slotConsumer.accept(x, y, 0);
    }

    protected void createInputSlots(AltarRecipe recipe, SlotConsumer slotConsumer) {
        var itemInputs = recipe.itemInputs();
        var entityInputs = recipe.entityInputs();
        var fakeEntityInputs = recipe.fakeEntityInputs();
        var inputSlots = itemInputs.size() + entityInputs.size() + fakeEntityInputs.size();

        for (var i = 0; i < inputSlots; i++) {
            var x = CENTER_X + (int) (Math.cos(i * 2 * Math.PI / inputSlots) * INPUT_RADIUS) - (SLOT_SIZE / 2);
            var y = CENTER_Y - 10 + (int) (Math.sin(i * 2 * Math.PI / inputSlots) * INPUT_RADIUS) - (SLOT_SIZE / 2);
            slotConsumer.accept(x, y, i);
        }
    }

    protected void createOutputSlots(AltarRecipe recipe, SlotConsumer slotConsumer) {
        var itemOutputs = recipe.itemOutputs();
        var entityOutputs = recipe.entityOutputs();
        var displayOutputs = recipe.displayOutputs();
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

    protected record CachedBlockPattern(ResourceLocation recipeId, BlockPatternCheck blockPattern) {

        public static final CachedBlockPattern NONE = new CachedBlockPattern(
            ResourceLocation.parse("none"),
            new BlockPatternCheck(List.of())
        );
    }
}

package com.almostreliable.summoningrituals.compat.viewer.emi;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.viewer.emi.entity.EntityEmiStack;
import com.almostreliable.summoningrituals.compat.viewer.emi.widget.CatalystSlotWidget;
import com.almostreliable.summoningrituals.compat.viewer.emi.widget.InvisibleSlotWidget;
import com.almostreliable.summoningrituals.compat.viewer.jei.entity.EntityIngredient;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AltarEmiRecipe implements EmiRecipe {

    private static final ResourceLocation TEXTURE = SummoningRituals.getRL(String.format("textures/gui/%s.png", Constants.RECIPE_VIEWER));
    private static final int TEXTURE_WIDTH = 188;
    private static final int TEXTURE_HEIGHT = 148;
    private static final int SLOT_SIZE = 16;
    private static final int CENTER_X = (TEXTURE_WIDTH - SLOT_SIZE) / 2;
    private static final int CENTER_Y = TEXTURE_HEIGHT / 2;
    private static final int INPUT_RADIUS = 46;

    private final RecipeHolder<AltarRecipe> recipeHolder;

    AltarEmiRecipe(RecipeHolder<AltarRecipe> recipeHolder) {
        this.recipeHolder = recipeHolder;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EmiPlugin.ALTAR_CATEGORY;
    }

    @Nullable
    @Override
    public ResourceLocation getId() {
        return recipeHolder.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        var recipe = recipeHolder.value();
        var inputs = new ArrayList<EmiIngredient>();

        for (var itemInput : recipe.itemInputs()) {
            inputs.add(EmiIngredient.of(itemInput.ingredient(), itemInput.count()));
        }
        for (var entityInput : recipe.entityInputs()) {
            var entityIngredient = new EntityIngredient(entityInput);
            inputs.add(EntityEmiStack.input(entityIngredient));
        }

        inputs.add(EmiIngredient.of(recipe.catalyst()));

        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        var recipe = recipeHolder.value();
        var itemOutputs = recipe.itemOutputs();
        var outputs = new ArrayList<EmiStack>();

        for (var itemOutput : itemOutputs) {
            outputs.add(EmiStack.of(itemOutput.item()));
        }
        for (var entityOutput : recipe.entityOutputs()) {
            var entityIngredient = new EntityIngredient(entityOutput.entityInfo());
            outputs.add(EntityEmiStack.output(entityIngredient));
        }

        return outputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        var recipe = recipeHolder.value();
        var catalysts = new ArrayList<EmiIngredient>();

        for (var entityInput : recipe.entityInputs()) {
            var entityIngredient = new EntityIngredient(entityInput);
            var egg = entityIngredient.getEgg();
            if (egg == null) continue;
            catalysts.add(EmiStack.of(egg));
        }
        for (var entityOutput : recipe.entityOutputs()) {
            var entityIngredient = new EntityIngredient(entityOutput.entityInfo());
            var egg = entityIngredient.getEgg();
            if (egg == null) continue;
            catalysts.add(EmiStack.of(egg));
        }

        return catalysts;
    }

    @Override
    public int getDisplayWidth() {
        return TEXTURE_WIDTH - SLOT_SIZE;
    }

    @Override
    public int getDisplayHeight() {
        return TEXTURE_HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(
            TEXTURE,
            0,
            0,
            getDisplayWidth(),
            getDisplayHeight(),
            0,
            0,
            getDisplayWidth(),
            getDisplayHeight(),
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT
        );
        var recipe = recipeHolder.value();
        var catalystSlot = new CatalystSlotWidget(EmiIngredient.of(recipe.catalyst()), CENTER_X - 8, CENTER_Y - 42);
        widgets.add(catalystSlot);
        createInputSlots(widgets, recipe);
        createOutputSlots(widgets, recipe);
    }

    private void createInputSlots(WidgetHolder widgets, AltarRecipe recipe) {
        var inputs = getInputs();
        var inputSlots = inputs.size() - 1;

        for (var i = 0; i < inputSlots; i++) {
            var x = CENTER_X + (int) (Math.cos(i * 2 * Math.PI / inputSlots) * INPUT_RADIUS) - (SLOT_SIZE / 2);
            var y = CENTER_Y - 10 + (int) (Math.sin(i * 2 * Math.PI / inputSlots) * INPUT_RADIUS) - (SLOT_SIZE / 2);
            widgets.add(new InvisibleSlotWidget(inputs.get(i), x, y));
        }
    }

    private void createOutputSlots(WidgetHolder widgets, AltarRecipe recipe) {
        var outputs = getOutputs();

        for (var i = 0; i < outputs.size(); i++) {
            var x = 2 + i * SLOT_SIZE;
            var y = TEXTURE_HEIGHT - 18;

            widgets.add(new InvisibleSlotWidget(outputs.get(i), x, y)).recipeContext(this);
        }
    }
}

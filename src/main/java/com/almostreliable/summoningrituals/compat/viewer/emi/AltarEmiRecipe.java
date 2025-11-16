package com.almostreliable.summoningrituals.compat.viewer.emi;

import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.common.RecipeViewerAltarLayout;
import com.almostreliable.summoningrituals.compat.viewer.emi.entity.EntityEmiStack;
import com.almostreliable.summoningrituals.compat.viewer.emi.widget.CatalystSlotWidget;
import com.almostreliable.summoningrituals.compat.viewer.emi.widget.InvisibleSlotWidget;
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

public class AltarEmiRecipe extends RecipeViewerAltarLayout implements EmiRecipe {

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
        return getWidth();
    }

    @Override
    public int getDisplayHeight() {
        return getHeight();
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

        createCatalystSlot((x, y, slot) ->
            widgets.add(new CatalystSlotWidget(EmiIngredient.of(recipe.catalyst()), x, y)));

        var inputs = getInputs();
        createInputSlots(
            recipe, (x, y, slot) ->
                widgets.add(new InvisibleSlotWidget(inputs.get(slot), x, y))
        );

        var outputs = getOutputs();
        createOutputSlots(
            recipe, (x, y, slot) ->
                widgets.add(new InvisibleSlotWidget(outputs.get(slot), x, y)).recipeContext(this)
        );
    }
}

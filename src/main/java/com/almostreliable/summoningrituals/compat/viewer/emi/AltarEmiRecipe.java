package com.almostreliable.summoningrituals.compat.viewer.emi;

import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.common.RecipeViewerAltarLayout;
import com.almostreliable.summoningrituals.compat.viewer.emi.entity.EntityEmiStack;
import com.almostreliable.summoningrituals.compat.viewer.emi.widget.InitiatorSlotWidget;
import com.almostreliable.summoningrituals.compat.viewer.emi.widget.InvisibleSlotWidget;
import com.almostreliable.summoningrituals.compat.viewer.emi.widget.StackWidget;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.condition.ConditionRegistry;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
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
            var entityIngredient = new EntityIngredient(entityInput.entityInfo());
            inputs.add(EntityEmiStack.input(entityIngredient));
        }
        for (var fakeEntityInput : recipe.fakeEntityInputs()) {
            inputs.add(EmiStack.of(fakeEntityInput.displayItem()));
        }

        inputs.add(EmiIngredient.of(recipe.initiator()));

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
            var entityIngredient = new EntityIngredient(entityInput.entityInfo());
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

        widgets.add(new InvisibleSlotWidget(
            EmiStack.of(Registration.ALTAR_BLOCK),
            CENTER_X - SLOT_SIZE / 2,
            CENTER_Y - SLOT_SIZE / 2 - 10
        ));

        var recipe = recipeHolder.value();

        createInitiatorSlot((x, y, slot) ->
            widgets.add(new InitiatorSlotWidget(EmiIngredient.of(recipe.initiator()), x, y)));

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

        var recipeConditions = recipe.startConditions();
        if (!recipeConditions.isEmpty()) {
            var conditionSlot = widgets.add(new StackWidget(EmiStack.of(Items.NETHER_STAR), 2, 2))
                .appendTooltip(SummoningLang.CONDITIONS.get().append(":").withStyle(ChatFormatting.GOLD));

            var recipeId = recipeHolder.id();
            if (cachedBlockPattern == null || !cachedBlockPattern.recipeId().equals(recipeId)) {
                cacheBlockPatternCondition(recipeId, recipeConditions);
            }

            if (cachedBlockPattern != CachedBlockPattern.NONE) {
                widgets.add(new StackWidget(EmiStack.of(Items.JIGSAW), 2, TEXTURE_HEIGHT - SLOT_SIZE * 2 - 5))
                    .appendClickHandler(this::onPreviewButtonClicked)
                    .appendTooltip(SummoningLang.BLOCK_PATTERN.get().withStyle(ChatFormatting.GOLD))
                    .appendTooltip(SummoningLang.PREVIEW_CLICK.get().withStyle(ChatFormatting.GRAY))
                    .appendTooltip(() -> ClientTooltipComponent.create(cachedBlockPattern.blockPattern().getTooltipComponent()));
            }

            for (var condition : recipeConditions) {
                var conditionTooltips = ConditionRegistry.getTooltip(condition);
                for (var conditionTooltip : conditionTooltips) {
                    conditionSlot.appendTooltip(conditionTooltip);
                }
            }
        }

        if (recipe.commands().isPresent()) {
            var slot = widgets.add(new StackWidget(
                EmiStack.of(Items.COMMAND_BLOCK),
                TEXTURE_WIDTH - SLOT_SIZE - 2,
                TEXTURE_HEIGHT - SLOT_SIZE * 2 - 5
            ));
            slot.appendTooltip(SummoningLang.COMMANDS.get().append(":").withStyle(ChatFormatting.GOLD));

            for (var commandTooltip : recipe.commands().get().getTooltip()) {
                slot.appendTooltip(commandTooltip);
            }
        }
    }
}

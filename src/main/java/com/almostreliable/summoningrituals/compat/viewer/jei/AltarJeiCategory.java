package com.almostreliable.summoningrituals.compat.viewer.jei;

import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.common.RecipeViewerAltarLayout;
import com.almostreliable.summoningrituals.compat.viewer.jei.entity.EntityIngredientJeiRenderer;
import com.almostreliable.summoningrituals.compat.viewer.jei.widget.JeiPatternClickListener;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.condition.ConditionRegistry;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

import com.mojang.datafixers.util.Either;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class AltarJeiCategory extends RecipeViewerAltarLayout implements IRecipeCategory<RecipeHolder<AltarRecipe>> {

    static final RecipeType<RecipeHolder<AltarRecipe>> TYPE = RecipeType.createFromVanilla(Registration.ALTAR_RECIPE_TYPE.get());

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable conditionIcon;
    private final IDrawable patternIcon;
    private final IDrawable commandsIcon;

    AltarJeiCategory(IGuiHelper guiHelper) {
        background = guiHelper.drawableBuilder(TEXTURE, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Registration.ALTAR_BLOCK.toStack());
        conditionIcon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.NETHER_STAR.getDefaultInstance());
        patternIcon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.STRUCTURE_BLOCK.getDefaultInstance());
        commandsIcon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.COMMAND_BLOCK.getDefaultInstance());
    }

    @Override
    public RecipeType<RecipeHolder<AltarRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Registration.ALTAR_BLOCK.get().getName();
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(
        RecipeHolder<AltarRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY
    ) {
        background.draw(guiGraphics);

        var recipeConditions = recipeHolder.value().startConditions();
        if (!recipeConditions.isEmpty()) {
            conditionIcon.draw(guiGraphics, 2, 2);

            var recipeId = recipeHolder.id();
            if (cachedBlockPattern == null || !cachedBlockPattern.recipeId().equals(recipeId)) {
                cacheBlockPatternCondition(recipeId, recipeConditions);
            }

            if (cachedBlockPattern != CachedBlockPattern.NONE) {
                patternIcon.draw(guiGraphics, 2, TEXTURE_HEIGHT - SLOT_SIZE * 2 - 5);
            }
        }

        var recipeCommands = recipeHolder.value().commands();
        if (recipeCommands.isPresent()) {
            commandsIcon.draw(guiGraphics, TEXTURE_WIDTH - SLOT_SIZE - 2, TEXTURE_HEIGHT - SLOT_SIZE * 2 - 5);
        }
    }

    @Override
    public void getTooltip(
        ITooltipBuilder tooltip, RecipeHolder<AltarRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY
    ) {
        var recipeConditions = recipeHolder.value().startConditions();
        if (!recipeConditions.isEmpty()) {
            if (mouseInSlot(mouseX, mouseY, 2, 2)) {
                tooltip.add(SummoningLang.CONDITIONS.get().append(":").withStyle(ChatFormatting.GOLD));
                for (var condition : recipeConditions) {
                    tooltip.addAll(ConditionRegistry.getTooltip(condition));
                }
                return;
            }

            if (cachedBlockPattern != null && cachedBlockPattern != CachedBlockPattern.NONE &&
                mouseInSlot(mouseX, mouseY, 2, TEXTURE_HEIGHT - SLOT_SIZE * 2 - 5)) {
                tooltip.add(SummoningLang.BLOCK_PATTERN.get().withStyle(ChatFormatting.GOLD));
                tooltip.add(SummoningLang.PREVIEW_CLICK.get().withStyle(ChatFormatting.GRAY));
                tooltip.add(cachedBlockPattern.blockPattern().getTooltipComponent());
            }
        }

        var recipeCommands = recipeHolder.value().commands();
        if (recipeCommands.isPresent() &&
            mouseInSlot(mouseX, mouseY, TEXTURE_WIDTH - SLOT_SIZE - 2, TEXTURE_HEIGHT - SLOT_SIZE * 2 - 5)) {
            tooltip.add(SummoningLang.COMMANDS.get().append(":").withStyle(ChatFormatting.GOLD));
            tooltip.addAll(recipeCommands.get().getTooltip());
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<AltarRecipe> recipe, IFocusGroup focuses) {
        if (cachedBlockPattern != CachedBlockPattern.NONE) {
            builder.addGuiEventListener(new JeiPatternClickListener(this));
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AltarRecipe> recipeHolder, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, CENTER_X - SLOT_SIZE / 2, CENTER_Y - SLOT_SIZE / 2 - 10)
            .addItemLike(Registration.ALTAR_BLOCK);

        var recipe = recipeHolder.value();
        createInitiatorSlot((x, y, slot) ->
            builder.addInputSlot(x, y)
                .addIngredients(recipe.initiator())
                .addRichTooltipCallback(AltarJeiCategory::constructInitiatorTooltip));

        createInputSlots(
            recipe, (x, y, slot) -> {
                var itemInputs = recipe.itemInputs();
                var entityInputs = recipe.entityInputs();
                if (slot < itemInputs.size()) {
                    // item inputs
                    var itemInput = itemInputs.get(slot);
                    var itemStacks = new ArrayList<ItemStack>();
                    for (var stack : itemInput.ingredient().getItems()) {
                        itemStacks.add(stack.copyWithCount(itemInput.count()));
                    }

                    builder.addInputSlot(x, y).addItemStacks(itemStacks);
                } else if (slot - itemInputs.size() < entityInputs.size()) {
                    // entity inputs
                    var entityInput = entityInputs.get(slot - itemInputs.size());
                    var entityIngredient = new EntityIngredient(entityInput.entityInfo());
                    var entityEgg = entityIngredient.getEgg();

                    builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .setCustomRenderer(JeiPlugin.ENTITY_INGREDIENT, EntityIngredientJeiRenderer.INPUT_RENDERER)
                        .addIngredient(JeiPlugin.ENTITY_INGREDIENT, entityIngredient);

                    if (entityEgg == null) return;
                    builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(entityEgg);
                } else {
                    // fake entity inputs
                    var fakeEntityInput = recipe.fakeEntityInputs().get(slot - itemInputs.size() - entityInputs.size());
                    builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStack(fakeEntityInput.displayItem());
                }
            }
        );

        createOutputSlots(
            recipe, (x, y, slot) -> {
                // item outputs
                var itemOutputs = recipe.itemOutputs();
                var entityOutputs = recipe.entityOutputs();
                if (slot < itemOutputs.size()) {
                    // item outputs
                    var stack = itemOutputs.get(slot).item();
                    builder.addOutputSlot(x, y).addItemStack(stack);
                } else if (slot - itemOutputs.size() < entityOutputs.size()) {
                    // entity outputs
                    var entityOutput = entityOutputs.get(slot - itemOutputs.size()).entityInfo();
                    var entityIngredient = new EntityIngredient(entityOutput);
                    var entityEgg = entityIngredient.getEgg();

                    builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                        .setCustomRenderer(JeiPlugin.ENTITY_INGREDIENT, EntityIngredientJeiRenderer.OUTPUT_RENDERER)
                        .addIngredient(JeiPlugin.ENTITY_INGREDIENT, entityIngredient);

                    if (entityEgg == null) return;
                    builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(entityEgg);
                } else {
                    // display outputs
                    var displayOutputs = recipe.displayOutputs();
                    var displayOutput = displayOutputs.get(slot - itemOutputs.size() - entityOutputs.size());
                    builder.addOutputSlot(x, y).addItemStack(displayOutput);
                }
            }
        );
    }

    private static void constructInitiatorTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
        var tooltipLines = tooltip.getLines();
        var stackTooltip = tooltipLines.getFirst().left().orElse(null);
        if (stackTooltip == null) return;

        var initiatorComponent = SummoningLang.INITIATOR.get()
            .append(": ")
            .withStyle(ChatFormatting.GOLD)
            .append(Component.literal(stackTooltip.getString()).withStyle(ChatFormatting.WHITE));
        tooltipLines.removeFirst();
        tooltipLines.addFirst(Either.left(SummoningLang.INSERT_LAST.get().withStyle(ChatFormatting.GRAY)));
        tooltipLines.addFirst(Either.left(initiatorComponent));
    }

    private static boolean mouseInSlot(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + SLOT_SIZE && mouseY >= y && mouseY <= y + SLOT_SIZE;
    }
}

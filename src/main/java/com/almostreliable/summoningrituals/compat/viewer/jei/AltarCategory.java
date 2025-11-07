package com.almostreliable.summoningrituals.compat.viewer.jei;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.viewer.jei.entity.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.jei.entity.EntityIngredientRenderer;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.condition.ConditionRegistry;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class AltarCategory implements IRecipeCategory<RecipeHolder<AltarRecipe>> {

    static final RecipeType<RecipeHolder<AltarRecipe>> TYPE = RecipeType.createFromVanilla(Registration.ALTAR_RECIPE_TYPE.get());
    private static final ResourceLocation TEXTURE = SummoningRituals.getRL(String.format("textures/gui/%s.png", Constants.RECIPE_VIEWER));
    private static final int TEXTURE_WIDTH = 188;
    private static final int TEXTURE_HEIGHT = 148;
    private static final int CENTER_X = (TEXTURE_WIDTH - 16) / 2;
    private static final int CENTER_Y = TEXTURE_HEIGHT / 2;
    private static final int INPUT_RADIUS = 46;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable conditionIcon;
    private final EntityIngredientRenderer entityIngredientRenderer = new EntityIngredientRenderer();

    AltarCategory(IGuiHelper guiHelper) {
        background = guiHelper.drawableBuilder(TEXTURE, 0, 0, TEXTURE_WIDTH - 16, TEXTURE_HEIGHT)
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Registration.ALTAR_BLOCK.toStack());
        conditionIcon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.NETHER_STAR.getDefaultInstance());
    }

    @Override
    public RecipeType<RecipeHolder<AltarRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return SummoningLang.ALTAR.get();
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return TEXTURE_WIDTH - 16;
    }

    @Override
    public int getHeight() {
        return TEXTURE_HEIGHT;
    }

    @Override
    public void draw(
        RecipeHolder<AltarRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY
    ) {
        background.draw(guiGraphics);

        var recipeConditions = recipeHolder.value().startConditions();
        if (recipeConditions.isEmpty()) return;

        conditionIcon.draw(guiGraphics, 2, 2);
    }

    @Override
    public void getTooltip(
        ITooltipBuilder tooltip, RecipeHolder<AltarRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY
    ) {
        var recipeConditions = recipeHolder.value().startConditions();
        if (recipeConditions.isEmpty()) return;

        if (mouseX >= 2 && mouseX <= 14 && mouseY >= 2 && mouseY <= 14) {
            tooltip.add(SummoningLang.CONDITIONS.get().append(":").withStyle(ChatFormatting.GOLD));
            for (var condition : recipeConditions) {
                tooltip.addAll(ConditionRegistry.getTooltip(condition));
            }
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AltarRecipe> recipeHolder, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, CENTER_X - 8, CENTER_Y - 18).addItemLike(Registration.ALTAR_BLOCK);

        var recipe = recipeHolder.value();
        builder.addInputSlot(CENTER_X - 8, CENTER_Y - 42)
            .addIngredients(recipe.catalyst())
            .addRichTooltipCallback(AltarCategory::constructCatalystTooltip);
        createInputSlots(builder, recipe);
        createOutputSlots(builder, recipe);
    }

    private void createInputSlots(IRecipeLayoutBuilder builder, AltarRecipe recipe) {
        var itemInputs = recipe.itemInputs();
        var entityInputs = recipe.entityInputs();
        var inputSlots = itemInputs.size() + entityInputs.size();

        for (var i = 0; i < inputSlots; i++) {
            var x = CENTER_X + (int) (Math.cos(i * 2 * Math.PI / inputSlots) * INPUT_RADIUS) - (16 / 2);
            var y = CENTER_Y - 10 + (int) (Math.sin(i * 2 * Math.PI / inputSlots) * INPUT_RADIUS) - (16 / 2);

            if (i < itemInputs.size()) {
                // item inputs
                // TODO: add custom sized ingredient renderer so it says "accepts tag #foo"
                var itemStacks = new ArrayList<ItemStack>();
                for (var stack : itemInputs.get(i).ingredient().getItems()) {
                    stack.setCount(itemInputs.get(i).count());
                    itemStacks.add(stack);
                }

                builder.addInputSlot(x, y).addItemStacks(itemStacks);
            } else {
                // entity inputs
                var entityInput = entityInputs.get(i - itemInputs.size());
                var entityIngredient = new EntityIngredient(entityInput.entityType(), entityInput.count());
                var entityEgg = entityIngredient.getEgg();

                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setCustomRenderer(JeiPlugin.ENTITY_INGREDIENT, entityIngredientRenderer)
                    .addIngredient(JeiPlugin.ENTITY_INGREDIENT, entityIngredient);
                if (entityEgg != null) builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(entityEgg);
            }
        }
    }

    private void createOutputSlots(IRecipeLayoutBuilder builder, AltarRecipe recipe) {
        // item outputs
        var itemOutputs = recipe.itemOutputs();
        for (var i = 0; i < itemOutputs.size(); i++) {
            var x = 2 + i * 16;
            var y = TEXTURE_HEIGHT - 18;

            var stack = itemOutputs.get(i).item();
            builder.addOutputSlot(x, y).addItemStack(stack);
        }

        // entity outputs
        var entityOutputs = recipe.entityOutputs();
        for (var i = 0; i < entityOutputs.size(); i++) {
            var x = 2 + (itemOutputs.size() + i) * 16;
            var y = TEXTURE_HEIGHT - 18;

            var entityOutput = entityOutputs.get(i).entityInfo();
            var entityIngredient = new EntityIngredient(
                entityOutput.entityInfo(),
                entityOutput.count(),
                entityOutput.data().orElseGet(CompoundTag::new)
            );
            var entityEgg = entityIngredient.getEgg();

            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                .setCustomRenderer(JeiPlugin.ENTITY_INGREDIENT, entityIngredientRenderer)
                .addIngredient(JeiPlugin.ENTITY_INGREDIENT, entityIngredient);
            if (entityEgg != null) builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(entityEgg);
        }
    }

    private static void constructCatalystTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
        var tooltipLines = tooltip.getLines();
        var stackTooltip = tooltipLines.getFirst().left().orElse(null);
        if (stackTooltip == null) return;

        var catalystComponent = SummoningLang.CATALYST.get()
            .append(": ")
            .withStyle(ChatFormatting.GOLD)
            .append(Component.literal(stackTooltip.getString()).withStyle(ChatFormatting.WHITE));
        tooltipLines.removeFirst();
        tooltipLines.addFirst(Either.left(SummoningLang.INSERT_LAST.get().withStyle(ChatFormatting.GRAY)));
        tooltipLines.addFirst(Either.left(catalystComponent));
    }
}

package com.almostreliable.summoningrituals.compat.viewer.jei;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.Registration;
import com.almostreliable.summoningrituals.compat.viewer.common.AltarCategory;
import com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.block.JEIBlockReferenceRenderer;
import com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.item.JEIAltarRenderer;
import com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.item.JEICatalystRenderer;
import com.almostreliable.summoningrituals.compat.viewer.jei.ingredient.mob.JEIMobRenderer;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.util.GameUtils;
import com.almostreliable.summoningrituals.util.TextUtils;
import com.almostreliable.summoningrituals.util.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.almostreliable.summoningrituals.Constants.ALTAR;
import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class AltarCategoryJEI extends AltarCategory<IDrawable, IIngredientRenderer<ItemStack>> implements IRecipeCategory<AltarRecipe> {

    static final RecipeType<AltarRecipe> TYPE = new RecipeType<>(Utils.getRL(ALTAR), AltarRecipe.class);

    private final IDrawable background;
    private final JEIBlockReferenceRenderer blockReferenceRenderer;
    private final JEIMobRenderer mobRenderer;

    AltarCategoryJEI(IGuiHelper guiHelper) {
        super(
            guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                Registration.ALTAR_ITEM.get().getDefaultInstance()
            ),
            new JEIAltarRenderer(BLOCK_SIZE),
            new JEICatalystRenderer(ITEM_SIZE)
        );

        background = guiHelper.drawableBuilder(TEXTURE, 0, 0, TEXTURE_WIDTH - SPRITE_SLOT_SIZE, TEXTURE_HEIGHT)
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();

        blockReferenceRenderer = new JEIBlockReferenceRenderer(BLOCK_SIZE);
        mobRenderer = new JEIMobRenderer(ITEM_SIZE);
    }

    @Override
    public RecipeType<AltarRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AltarRecipe recipe, IFocusGroup focuses) {
        // block below
        if (recipe.blockBelow() != null) {
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, CENTER_X - BLOCK_SLOT_SIZE / 2, RENDER_Y - 3)
                .setCustomRenderer(AlmostJEI.BLOCK_REFERENCE, blockReferenceRenderer)
                .addIngredient(AlmostJEI.BLOCK_REFERENCE, recipe.blockBelow());
        }

        // catalyst
        builder.addSlot(RecipeIngredientRole.INPUT, CENTER_X - ITEM_SLOT_SIZE / 2, RENDER_Y - 32)
            .setCustomRenderer(VanillaTypes.ITEM_STACK, catalystRenderer)
            .addIngredients(recipe.catalyst());

        // inputs
        handleInputs(
            0, 0, recipe,
            (x, y, inputs) -> builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStacks(inputs),
            (x, y, mob, egg) -> {
                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .setCustomRenderer(AlmostJEI.MOB, mobRenderer)
                    .addIngredient(AlmostJEI.MOB, mob);
                if (egg != null) {
                    builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(egg.getDefaultInstance());
                }
            }
        );

        // outputs
        handleOutputs(
            0, 0, recipe,
            (x, y, output) -> builder.addSlot(RecipeIngredientRole.OUTPUT, x, y).addItemStack(output),
            (x, y, mob, egg) -> {
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setCustomRenderer(AlmostJEI.MOB, mobRenderer)
                    .addIngredient(AlmostJEI.MOB, mob);
                if (egg != null) {
                    builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(egg.getDefaultInstance());
                }
            }
        );
    }

    @Override
    public void draw(
        AltarRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY
    ) {
        PoseStack stack = guiGraphics.pose();

        // altar
        stack.pushPose();
        {
            var altarY = RENDER_Y - BLOCK_SLOT_SIZE / 2f - (recipe.blockBelow() == null ? 0 : 4);
            stack.translate(CENTER_X - BLOCK_SLOT_SIZE / 2f, altarY, 0);
            // noinspection DataFlowIssue
            altarRenderer.render(guiGraphics, null);
        }
        stack.popPose();

        // labels
        drawLabel(
            guiGraphics,
            f("{}:", TextUtils.translateAsString(Constants.LABEL, Constants.OUTPUTS)),
            GameUtils.ANCHOR.BOTTOM_LEFT,
            2,
            128,
            0x36_A400
        );
        if (!recipe.sacrifices().isEmpty()) {
            drawLabel(
                guiGraphics,
                f("{}:", TextUtils.translateAsString(Constants.LABEL, Constants.REGION)),
                GameUtils.ANCHOR.TOP_LEFT,
                1,
                1,
                0x00_A2FF
            );
            drawLabel(
                guiGraphics,
                recipe.sacrifices().getDisplayRegion(),
                GameUtils.ANCHOR.TOP_LEFT,
                1,
                11,
                0xFF_FFFF
            );
        }

        // condition sprites
        var sprites = conditionSpriteWidgets.stream().filter(s -> s.test(recipe)).toList();
        var spriteOffset = 0;
        for (var sprite : sprites) {
            sprite.render(guiGraphics, 0, spriteOffset);
            spriteOffset += SPRITE_SLOT_SIZE + 1;
        }
    }

    @Override
    public List<Component> getTooltipStrings(AltarRecipe recipe, IRecipeSlotsView slotsView, double mX, double mY) {
        return getTooltip(recipe, 0, 0, mX, mY);
    }
}

package com.almostreliable.summoningrituals.compat.viewer.rei;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.Registration;
import com.almostreliable.summoningrituals.compat.viewer.common.AltarCategory;
import com.almostreliable.summoningrituals.compat.viewer.rei.ingredient.item.REIAltarRenderer;
import com.almostreliable.summoningrituals.compat.viewer.rei.ingredient.item.REICatalystRenderer;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.util.GameUtils;
import com.almostreliable.summoningrituals.util.TextUtils;
import com.almostreliable.summoningrituals.util.Utils;
import com.google.common.collect.ImmutableList;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.almostreliable.summoningrituals.Constants.ALTAR;
import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class AltarCategoryREI extends AltarCategory<Renderer, EntryRenderer<ItemStack>> implements DisplayCategory<AltarCategoryREI.AltarDisplay> {

    static final CategoryIdentifier<AltarDisplay> ID = CategoryIdentifier.of(Utils.getRL(ALTAR));
    private static final int BOUNDS_PADDING = 8;

    AltarCategoryREI() {
        super(
            EntryStacks.of(Registration.ALTAR_ITEM.get()),
            new REIAltarRenderer(BLOCK_SIZE),
            new REICatalystRenderer(ITEM_SIZE)
        );
    }

    @Override
    public CategoryIdentifier<? extends AltarDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public List<Widget> setupDisplay(AltarDisplay display, Rectangle bounds) {
        var widgets = new ArrayList<Widget>();
        widgets.add(Widgets.createRecipeBase(bounds));

        var offsetX = bounds.x + BOUNDS_PADDING / 2;
        var offsetY = bounds.y + BOUNDS_PADDING / 2;
        widgets.add(Widgets.createTexturedWidget(
            TEXTURE,
            offsetX,
            offsetY,
            0,
            0,
            TEXTURE_WIDTH - SPRITE_SLOT_SIZE,
            TEXTURE_HEIGHT,
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT
        ));

        var recipe = display.recipe;

        // block below
        if (recipe.blockBelow() != null) {
            var blockBelowX = offsetX + CENTER_X - BLOCK_SLOT_SIZE / 2;
            var blockBelowY = offsetY + RENDER_Y - 3;
            widgets.add(
                Widgets.createSlot(new Rectangle(blockBelowX, blockBelowY, BLOCK_SIZE, BLOCK_SIZE))
                    .entry(EntryStack.of(AlmostREI.BLOCK_REFERENCE, recipe.blockBelow()))
                    .markInput()
                    .disableBackground()
            );
        }

        // altar
        ClientEntryStacks.setRenderer(EntryStacks.of(altar), altarRenderer);
        var altarX = offsetX + CENTER_X - BLOCK_SLOT_SIZE / 2;
        var altarY = offsetY + RENDER_Y - BLOCK_SLOT_SIZE / 2 - (recipe.blockBelow() == null ? 0 : 4);
        widgets.add(
            Widgets.createSlot(new Rectangle(altarX, altarY, BLOCK_SIZE, BLOCK_SIZE))
                .entry(EntryStacks.of(altar))
                .disableBackground()
                .disableHighlight()
                .disableTooltips()
        );

        // labels
        widgets.add(
            labelWidget(
                f("{}:", TextUtils.translateAsString(Constants.LABEL, Constants.OUTPUTS)),
                GameUtils.ANCHOR.BOTTOM_LEFT,
                offsetX + 2,
                offsetY + 128,
                0x36_A400
            )
        );
        if (!recipe.sacrifices().isEmpty()) {
            widgets.add(
                labelWidget(
                    f("{}:", TextUtils.translateAsString(Constants.LABEL, Constants.REGION)),
                    GameUtils.ANCHOR.TOP_LEFT,
                    offsetX + 1,
                    offsetY + 1,
                    0x00_A2FF
                )
            );
            widgets.add(
                labelWidget(
                    recipe.sacrifices().getDisplayRegion(),
                    GameUtils.ANCHOR.TOP_LEFT,
                    offsetX + 1,
                    offsetY + 11,
                    0xFF_FFFF
                )
            );
        }

        // condition sprites
        var sprites = conditionSpriteWidgets.stream().filter(s -> s.test(recipe)).toList();
        var spriteOffset = 0;
        for (var sprite : sprites) {
            int spriteOffsetF = spriteOffset;
            widgets.add(
                Widgets.createDrawableWidget((guiGraphics, mX, mY, partial) ->
                    sprite.render(guiGraphics, offsetX, offsetY + spriteOffsetF)
                )
            );
            spriteOffset += SPRITE_SLOT_SIZE + 1;
        }

        // catalyst
        var catalystEntry = EntryIngredients.ofIngredient(recipe.catalyst());
        catalystEntry.forEach(e -> ClientEntryStacks.setRenderer(e, catalystRenderer));
        var catalystX = offsetX + CENTER_X - ITEM_SLOT_SIZE / 2;
        var catalystY = offsetY + RENDER_Y - 32;
        widgets.add(
            Widgets.createSlot(new Point(catalystX, catalystY))
                .entries(catalystEntry)
                .markInput()
                .disableBackground()
        );

        // inputs
        handleInputs(
            offsetX, offsetY, recipe,
            (x, y, inputs) -> widgets.add(
                Widgets.createSlot(new Point(x, y))
                    .entries(inputs.stream().map(EntryStacks::of).toList())
                    .markInput()
                    .disableBackground()
            ),
            (x, y, mob, egg) -> widgets.add(
                Widgets.createSlot(new Point(x, y))
                    .entry(EntryStack.of(AlmostREI.MOB, mob))
                    .markInput()
                    .disableBackground()
            )
        );

        // outputs
        handleOutputs(
            offsetX, offsetY, recipe,
            (x, y, output) -> widgets.add(
                Widgets.createSlot(new Point(x, y))
                    .entry(EntryStacks.of(output))
                    .markOutput()
                    .disableBackground()
            ),
            (x, y, mob, egg) -> widgets.add(
                Widgets.createSlot(new Point(x, y))
                    .entry(EntryStack.of(AlmostREI.MOB, mob))
                    .markOutput()
                    .disableBackground()
            )
        );

        // tooltips
        widgets.add(Widgets.createDrawableWidget((guiGraphics, mX, mY, partial) -> {
            var tooltip = getTooltip(recipe, offsetX, offsetY, mX, mY);
            if (!tooltip.isEmpty()) {
                Tooltip.create(tooltip).queue();
            }
        }));

        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return TEXTURE_HEIGHT + 8;
    }

    @Override
    public int getDisplayWidth(AltarDisplay display) {
        return TEXTURE_WIDTH - SPRITE_SLOT_SIZE + 8;
    }

    private Widget labelWidget(String text, GameUtils.ANCHOR anchor, int x, int y, int color) {
        return Widgets.createDrawableWidget((guiGraphics, mX, mY, partial) ->
            drawLabel(guiGraphics, text, anchor, x, y, color)
        );
    }

    public static class AltarDisplay implements Display {

        private final AltarRecipe recipe;
        private final List<EntryIngredient> inputs;
        private final List<EntryIngredient> outputs;

        AltarDisplay(AltarRecipe recipe) {
            this.recipe = recipe;
            this.inputs = createInputs(recipe);
            this.outputs = createOutputs(recipe);
        }

        private static List<EntryIngredient> createInputs(AltarRecipe recipe) {
            List<EntryIngredient> inputIngredients = new ArrayList<>();
            handleInputs(
                0, 0, recipe,
                (x, y, inputs) -> inputIngredients.add(EntryIngredients.ofItemStacks(inputs)),
                (x, y, mob, egg) -> {
                    inputIngredients.add(EntryIngredient.of(EntryStack.of(AlmostREI.MOB, mob)));
                    if (egg != null) {
                        inputIngredients.add(EntryIngredients.of(egg));
                    }
                }
            );
            inputIngredients.add(EntryIngredients.ofIngredient(recipe.catalyst()));

            return ImmutableList.copyOf(inputIngredients);
        }

        private static List<EntryIngredient> createOutputs(AltarRecipe recipe) {
            List<EntryIngredient> outputIngredients = new ArrayList<>();
            handleOutputs(
                0, 0, recipe,
                (x, y, output) -> outputIngredients.add(EntryIngredients.of(output)),
                (x, y, mob, egg) -> {
                    outputIngredients.add(EntryIngredient.of(EntryStack.of(AlmostREI.MOB, mob)));
                    if (egg != null) {
                        outputIngredients.add(EntryIngredients.of(egg));
                    }
                }
            );

            return ImmutableList.copyOf(outputIngredients);
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return ID;
        }

        @Override
        public Optional<ResourceLocation> getDisplayLocation() {
            return Optional.of(recipe.getId());
        }

        @Override
        public List<EntryIngredient> getInputEntries() {
            return inputs;
        }

        @Override
        public List<EntryIngredient> getOutputEntries() {
            return outputs;
        }
    }
}

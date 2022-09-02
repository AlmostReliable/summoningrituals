package com.almostreliable.summoningrituals.compat.jei;

import com.almostreliable.summoningrituals.Setup;
import com.almostreliable.summoningrituals.compat.jei.AlmostJEI.AlmostTypes;
import com.almostreliable.summoningrituals.compat.jei.ingredient.block.BlockStateRenderer;
import com.almostreliable.summoningrituals.compat.jei.ingredient.entity.EntityIngredient;
import com.almostreliable.summoningrituals.compat.jei.ingredient.item.SizedItemRenderer;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.AltarRecipe.WEATHER;
import com.almostreliable.summoningrituals.recipe.RecipeOutputs.OutputType;
import com.almostreliable.summoningrituals.util.GameUtils;
import com.almostreliable.summoningrituals.util.TextUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.almostreliable.summoningrituals.Constants.ALTAR;
import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class AltarCategory implements IRecipeCategory<AltarRecipe> {

    public static final RecipeType<AltarRecipe> TYPE = new RecipeType<>(TextUtils.getRL(ALTAR), AltarRecipe.class);
    private static final ResourceLocation TEXTURE = TextUtils.getRL(f("textures/jei/{}.png", ALTAR));
    private static final int TEXTURE_WIDTH = 194;
    private static final int TEXTURE_HEIGHT = 148;
    private static final int BIG_SLOT_SIZE = 22;
    private static final int SMALL_SLOT_SIZE = 18;
    private static final int WEATHER_SLOT_SIZE = 16;
    private static final int WEATHER_ICON_SIZE = 12;
    private static final int CENTER_X = (TEXTURE_WIDTH - BIG_SLOT_SIZE) / 2;
    private static final int RENDER_Y = 62;
    private static final int SLOT_RADIUS = 48;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable blockBelowSlot;
    private final IDrawable catalystSlot;
    private final IDrawable weatherSlot;
    private final IDrawable sunIcon;
    private final IDrawable rainIcon;
    private final IDrawable thunderIcon;
    private final BlockStateRenderer blockStateRenderer;
    private final SizedItemRenderer sizedItemRenderer;

    public AltarCategory(IGuiHelper guiHelper) {
        background = guiHelper.drawableBuilder(TEXTURE, 0, 0, TEXTURE_WIDTH - BIG_SLOT_SIZE, TEXTURE_HEIGHT)
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Setup.ALTAR_ITEM.get()));
        blockBelowSlot = guiHelper.drawableBuilder(
                TEXTURE,
                TEXTURE_WIDTH - BIG_SLOT_SIZE,
                0,
                BIG_SLOT_SIZE,
                BIG_SLOT_SIZE
            )
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        catalystSlot = guiHelper.drawableBuilder(
                TEXTURE,
                TEXTURE_WIDTH - BIG_SLOT_SIZE,
                BIG_SLOT_SIZE,
                SMALL_SLOT_SIZE,
                SMALL_SLOT_SIZE
            )
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        weatherSlot = guiHelper.drawableBuilder(
                TEXTURE,
                TEXTURE_WIDTH - BIG_SLOT_SIZE,
                BIG_SLOT_SIZE + SMALL_SLOT_SIZE,
                WEATHER_SLOT_SIZE,
                WEATHER_SLOT_SIZE
            )
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        sunIcon = guiHelper.drawableBuilder(
                TEXTURE,
                TEXTURE_WIDTH - BIG_SLOT_SIZE,
                BIG_SLOT_SIZE + SMALL_SLOT_SIZE + WEATHER_SLOT_SIZE,
                WEATHER_ICON_SIZE,
                WEATHER_ICON_SIZE
            )
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        rainIcon = guiHelper.drawableBuilder(
                TEXTURE,
                TEXTURE_WIDTH - BIG_SLOT_SIZE,
                BIG_SLOT_SIZE + SMALL_SLOT_SIZE + 2 * WEATHER_SLOT_SIZE - 2,
                WEATHER_ICON_SIZE,
                WEATHER_ICON_SIZE
            )
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        thunderIcon = guiHelper.drawableBuilder(
                TEXTURE,
                TEXTURE_WIDTH - BIG_SLOT_SIZE,
                BIG_SLOT_SIZE + SMALL_SLOT_SIZE + 3 * WEATHER_SLOT_SIZE - 4,
                WEATHER_ICON_SIZE,
                WEATHER_ICON_SIZE
            )
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        blockStateRenderer = new BlockStateRenderer(BIG_SLOT_SIZE - 2);
        sizedItemRenderer = new SizedItemRenderer(BIG_SLOT_SIZE - 2);
    }

    @Override
    public RecipeType<AltarRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return TextUtils.translate("block", ALTAR);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AltarRecipe recipe, IFocusGroup focuses) {
        var yOffset = BIG_SLOT_SIZE / 2 + 1;
        if (recipe.getBlockBelow() != null) {
            builder.addSlot(
                    RecipeIngredientRole.RENDER_ONLY,
                    CENTER_X - BIG_SLOT_SIZE / 2 + 1,
                    RENDER_Y + yOffset
                )
                .setCustomRenderer(AlmostTypes.BLOCK_STATE, blockStateRenderer)
                .addIngredient(AlmostTypes.BLOCK_STATE, recipe.getBlockBelow().getDisplayState());
            yOffset = 1;
        }
        builder.addSlot(
                RecipeIngredientRole.RENDER_ONLY,
                CENTER_X - BIG_SLOT_SIZE / 2 + 1,
                RENDER_Y - BIG_SLOT_SIZE / 2 + yOffset
            )
            .setCustomRenderer(VanillaTypes.ITEM_STACK, sizedItemRenderer)
            .addItemStack(new ItemStack(Setup.ALTAR_ITEM.get()));
        builder.addSlot(
                RecipeIngredientRole.INPUT,
                CENTER_X - SMALL_SLOT_SIZE / 2 + 1,
                RENDER_Y - BIG_SLOT_SIZE / 2 - SMALL_SLOT_SIZE + yOffset
            )
            .addIngredients(recipe.getCatalyst());

        var inputs = recipe.getInputs();
        var sacrifices = recipe.getSacrifices();
        var ringSlots = inputs.size() + sacrifices.size();

        for (var i = 0; i < ringSlots; i++) {
            var x = CENTER_X + (int) (Math.cos(i * 2 * Math.PI / ringSlots) * SLOT_RADIUS) - SMALL_SLOT_SIZE / 2;
            var y = RENDER_Y + (int) (Math.sin(i * 2 * Math.PI / ringSlots) * SLOT_RADIUS) - SMALL_SLOT_SIZE / 2;

            if (i < inputs.size()) {
                List<ItemStack> inputStacks = new ArrayList<>();
                for (var stack : inputs.get(i).ingredient().getItems()) {
                    stack.setCount(inputs.get(i).count());
                    inputStacks.add(stack);
                }
                builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStacks(inputStacks);
            } else {
                var sacrifice = sacrifices.get(i - inputs.size());
                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addIngredient(AlmostTypes.ENTITY, new EntityIngredient(sacrifice.mob(), sacrifice.count()));
            }
        }

        recipe.getOutputs().forEach((type, output, i) -> {
            var x = 2 + i * (SMALL_SLOT_SIZE - 1);
            var y = 130;

            if (type == OutputType.ITEM) {
                builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStack((ItemStack) output.getOutput());
            } else if (type == OutputType.MOB) {
                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addIngredient(
                        AlmostTypes.ENTITY,
                        new EntityIngredient((EntityType<?>) output.getOutput(), output.getCount())
                    );
            }
        });
    }

    @Override
    public void draw(
        AltarRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY
    ) {
        var yOffset = BIG_SLOT_SIZE / 2;
        if (recipe.getBlockBelow() != null) {
            blockBelowSlot.draw(
                stack,
                CENTER_X - BIG_SLOT_SIZE / 2,
                RENDER_Y + BIG_SLOT_SIZE / 2
            );
            yOffset = 0;
        }
        catalystSlot.draw(
            stack,
            CENTER_X - SMALL_SLOT_SIZE / 2,
            RENDER_Y - BIG_SLOT_SIZE / 2 - SMALL_SLOT_SIZE + yOffset
        );
        // TODO: make translation keys
        GameUtils.renderText(stack, "Output:", GameUtils.ANCHOR.BOTTOM_LEFT, 2, 128, 1, 0x36_A400);
        if (!recipe.getSacrifices().isEmpty()) {
            GameUtils.renderText(stack, "Range:", GameUtils.ANCHOR.TOP_LEFT, 1, 1, 1, 0x00_A2FF);
            GameUtils.renderText(
                stack,
                recipe.getSacrifices().getDisplayRegion(),
                GameUtils.ANCHOR.TOP_LEFT,
                1,
                11,
                1,
                0xFF_FFFF
            );
        }
        if (recipe.getWeather() != WEATHER.ANY) {
            weatherSlot.draw(stack, TEXTURE_WIDTH - BIG_SLOT_SIZE - WEATHER_SLOT_SIZE - 1, 1);
            switch (recipe.getWeather()) {
                case SUN -> sunIcon.draw(stack, TEXTURE_WIDTH - BIG_SLOT_SIZE - WEATHER_SLOT_SIZE + 1, 3);
                case RAIN -> rainIcon.draw(stack, TEXTURE_WIDTH - BIG_SLOT_SIZE - WEATHER_SLOT_SIZE + 1, 3);
                case THUNDER -> thunderIcon.draw(stack, TEXTURE_WIDTH - BIG_SLOT_SIZE - WEATHER_SLOT_SIZE + 1, 3);
                default -> throw new IllegalStateException("Unexpected value: " + recipe.getWeather());
            }
        }
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getUid() {
        return TYPE.getUid();
    }

    @SuppressWarnings("removal")
    @Override
    public Class<? extends AltarRecipe> getRecipeClass() {
        return TYPE.getRecipeClass();
    }
}

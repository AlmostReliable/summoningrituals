package com.almostreliable.summoningrituals.compat.jei;

import com.almostreliable.summoningrituals.Setup;
import com.almostreliable.summoningrituals.compat.jei.AlmostJEI.AlmostTypes;
import com.almostreliable.summoningrituals.compat.jei.ingredient.block.BlockStateRenderer;
import com.almostreliable.summoningrituals.compat.jei.ingredient.entity.EntityIngredient;
import com.almostreliable.summoningrituals.compat.jei.ingredient.item.SizedItemRenderer;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.AltarRecipe.DAY_TIME;
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
import net.minecraft.ChatFormatting;
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
    private static final int SPRITE_SLOT_SIZE = 16;
    private static final int SPRITE_SIZE = 14;
    private static final int CENTER_X = (TEXTURE_WIDTH - BIG_SLOT_SIZE) / 2;
    private static final int RENDER_Y = 62;
    private static final int SLOT_RADIUS = 48;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable blockBelowSlot;
    private final IDrawable catalystSlot;
    private final IDrawable spriteSlot;
    private final IDrawable daySprite;
    private final IDrawable nightSprite;
    private final IDrawable sunSprite;
    private final IDrawable rainSprite;
    private final IDrawable thunderSprite;
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
        spriteSlot = guiHelper.drawableBuilder(
                TEXTURE,
                TEXTURE_WIDTH - BIG_SLOT_SIZE,
                BIG_SLOT_SIZE + SMALL_SLOT_SIZE,
                SPRITE_SLOT_SIZE,
                SPRITE_SLOT_SIZE
            )
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        daySprite = guiHelper.drawableBuilder(
                TEXTURE,
                TEXTURE_WIDTH - BIG_SLOT_SIZE,
                BIG_SLOT_SIZE + SMALL_SLOT_SIZE + SPRITE_SLOT_SIZE,
                SPRITE_SIZE,
                SPRITE_SIZE
            )
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        nightSprite = guiHelper.drawableBuilder(
                TEXTURE,
                TEXTURE_WIDTH - BIG_SLOT_SIZE,
                BIG_SLOT_SIZE + SMALL_SLOT_SIZE + SPRITE_SLOT_SIZE + SPRITE_SIZE,
                SPRITE_SIZE,
                SPRITE_SIZE
            )
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        sunSprite = guiHelper.drawableBuilder(
                TEXTURE,
                TEXTURE_WIDTH - BIG_SLOT_SIZE,
                BIG_SLOT_SIZE + SMALL_SLOT_SIZE + SPRITE_SLOT_SIZE + 2 * SPRITE_SIZE,
                SPRITE_SIZE,
                SPRITE_SIZE
            )
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        rainSprite = guiHelper.drawableBuilder(
                TEXTURE,
                TEXTURE_WIDTH - BIG_SLOT_SIZE,
                BIG_SLOT_SIZE + SMALL_SLOT_SIZE + SPRITE_SLOT_SIZE + 3 * SPRITE_SIZE,
                SPRITE_SIZE,
                SPRITE_SIZE
            )
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        thunderSprite = guiHelper.drawableBuilder(
                TEXTURE,
                TEXTURE_WIDTH - BIG_SLOT_SIZE,
                BIG_SLOT_SIZE + SMALL_SLOT_SIZE + SPRITE_SLOT_SIZE + 4 * SPRITE_SIZE,
                SPRITE_SIZE,
                SPRITE_SIZE
            )
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        blockStateRenderer = new BlockStateRenderer(BIG_SLOT_SIZE - 2);
        sizedItemRenderer = new SizedItemRenderer(BIG_SLOT_SIZE - 2);
    }

    private Component requirementTooltip(String translationKey, String value) {
        return TextUtils.translate("tooltip", translationKey, ChatFormatting.AQUA)
            .append(": ")
            .append(TextUtils.translate(translationKey, value.toLowerCase(), ChatFormatting.WHITE));
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
        GameUtils.renderText(
            stack,
            f("{}:", TextUtils.translateAsString("label", "output")),
            GameUtils.ANCHOR.BOTTOM_LEFT,
            2,
            128,
            1,
            0x36_A400
        );
        if (!recipe.getSacrifices().isEmpty()) {
            GameUtils.renderText(
                stack,
                f("{}:", TextUtils.translateAsString("label", "range")),
                GameUtils.ANCHOR.TOP_LEFT,
                1,
                1,
                1,
                0x00_A2FF
            );
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
        var spriteOffset = 1;
        if (recipe.getDayTime() != DAY_TIME.ANY) {
            spriteSlot.draw(stack, TEXTURE_WIDTH - BIG_SLOT_SIZE - SPRITE_SLOT_SIZE - 1, spriteOffset);
            if (recipe.getDayTime() == DAY_TIME.DAY) {
                daySprite.draw(stack, TEXTURE_WIDTH - BIG_SLOT_SIZE - SPRITE_SLOT_SIZE, spriteOffset + 1);
            } else if (recipe.getDayTime() == DAY_TIME.NIGHT) {
                nightSprite.draw(stack, TEXTURE_WIDTH - BIG_SLOT_SIZE - SPRITE_SLOT_SIZE, spriteOffset + 1);
            }
            spriteOffset += SPRITE_SLOT_SIZE + 1;
        }
        if (recipe.getWeather() != WEATHER.ANY) {
            spriteSlot.draw(stack, TEXTURE_WIDTH - BIG_SLOT_SIZE - SPRITE_SLOT_SIZE - 1, spriteOffset);
            switch (recipe.getWeather()) {
                case CLEAR -> sunSprite.draw(stack, TEXTURE_WIDTH - BIG_SLOT_SIZE - SPRITE_SLOT_SIZE, spriteOffset + 1);
                case RAIN -> rainSprite.draw(stack, TEXTURE_WIDTH - BIG_SLOT_SIZE - SPRITE_SLOT_SIZE, spriteOffset + 1);
                case THUNDER ->
                    thunderSprite.draw(stack, TEXTURE_WIDTH - BIG_SLOT_SIZE - SPRITE_SLOT_SIZE, spriteOffset + 1);
                default -> throw new IllegalStateException("Unexpected value: " + recipe.getWeather());
            }
        }
    }

    @Override
    public List<Component> getTooltipStrings(
        AltarRecipe recipe, IRecipeSlotsView recipeSlotsView, double mX, double mY
    ) {
        List<Component> tooltip = new ArrayList<>();
        if (!recipe.getSacrifices().isEmpty() && GameUtils.isWithinBounds(mX, mY, 1, 1, 30, 20)) {
            tooltip.add(TextUtils.translate("tooltip", "sacrifice_range", ChatFormatting.WHITE));
        }
        if (GameUtils.isWithinBounds(
            mX,
            mY,
            TEXTURE_WIDTH - BIG_SLOT_SIZE - SPRITE_SLOT_SIZE - 1,
            1,
            SPRITE_SLOT_SIZE,
            SPRITE_SLOT_SIZE
        )) {
            if (recipe.getDayTime() != DAY_TIME.ANY) {
                tooltip.add(requirementTooltip("day_time", recipe.getDayTime().name()));
            } else if (recipe.getWeather() != WEATHER.ANY) {
                tooltip.add(requirementTooltip("weather", recipe.getWeather().name()));
            }
        }
        if (GameUtils.isWithinBounds(
            mX,
            mY,
            TEXTURE_WIDTH - BIG_SLOT_SIZE - SPRITE_SLOT_SIZE - 1,
            SPRITE_SLOT_SIZE + 2,
            SPRITE_SLOT_SIZE,
            SPRITE_SLOT_SIZE
        )) {
            if (recipe.getWeather() != WEATHER.ANY) {
                tooltip.add(requirementTooltip("weather", recipe.getWeather().name()));
            }
        }
        return tooltip;
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

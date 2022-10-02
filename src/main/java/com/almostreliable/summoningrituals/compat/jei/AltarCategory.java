package com.almostreliable.summoningrituals.compat.jei;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.Setup;
import com.almostreliable.summoningrituals.compat.jei.AlmostJEI.AlmostTypes;
import com.almostreliable.summoningrituals.compat.jei.ingredient.block.BlockReferenceRenderer;
import com.almostreliable.summoningrituals.compat.jei.ingredient.item.AltarRenderer;
import com.almostreliable.summoningrituals.compat.jei.ingredient.item.CatalystRenderer;
import com.almostreliable.summoningrituals.compat.jei.ingredient.mob.MobIngredient;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.AltarRecipe.DAY_TIME;
import com.almostreliable.summoningrituals.recipe.AltarRecipe.WEATHER;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs.OutputType;
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
import static com.almostreliable.summoningrituals.Constants.JEI;
import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class AltarCategory implements IRecipeCategory<AltarRecipe> {

    static final RecipeType<AltarRecipe> TYPE = new RecipeType<>(TextUtils.getRL(ALTAR), AltarRecipe.class);
    private static final ResourceLocation TEXTURE = TextUtils.getRL(f("textures/{}/{}.png", JEI, ALTAR));
    private static final int TEXTURE_WIDTH = 188;
    private static final int TEXTURE_HEIGHT = 148;

    private static final int ITEM_SLOT_SIZE = 18;
    private static final int BLOCK_SLOT_SIZE = 22;
    private static final int SPRITE_SLOT_SIZE = 16;
    private static final int SPRITE_SIZE = SPRITE_SLOT_SIZE - 2;

    private static final int CENTER_X = (TEXTURE_WIDTH - SPRITE_SLOT_SIZE) / 2 + 1;
    private static final int RENDER_Y = 64;
    private static final int INPUT_RADIUS = 47;

    private final AltarRenderer altarRenderer;
    private final CatalystRenderer catalystRenderer;
    private final BlockReferenceRenderer blockReferenceRenderer;

    private final IDrawable background;
    private final IDrawable icon;

    private final IDrawable spriteSlot;
    private final IDrawable daySprite;
    private final IDrawable nightSprite;
    private final IDrawable sunSprite;
    private final IDrawable rainSprite;
    private final IDrawable thunderSprite;

    AltarCategory(IGuiHelper guiHelper) {
        background = guiHelper.drawableBuilder(TEXTURE, 0, 0, TEXTURE_WIDTH - SPRITE_SLOT_SIZE, TEXTURE_HEIGHT)
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Setup.ALTAR_ITEM.get()));

        altarRenderer = new AltarRenderer(BLOCK_SLOT_SIZE - 2);
        catalystRenderer = new CatalystRenderer(ITEM_SLOT_SIZE - 2);
        blockReferenceRenderer = new BlockReferenceRenderer(BLOCK_SLOT_SIZE - 2);

        spriteSlot = spriteTexture(guiHelper, 0, SPRITE_SLOT_SIZE, SPRITE_SLOT_SIZE);
        daySprite = spriteTexture(guiHelper, SPRITE_SLOT_SIZE, SPRITE_SIZE, SPRITE_SIZE);
        nightSprite = spriteTexture(guiHelper, SPRITE_SLOT_SIZE + SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);
        sunSprite = spriteTexture(guiHelper, SPRITE_SLOT_SIZE + 2 * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);
        rainSprite = spriteTexture(guiHelper, SPRITE_SLOT_SIZE + 3 * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);
        thunderSprite = spriteTexture(guiHelper, SPRITE_SLOT_SIZE + 4 * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);
    }

    private IDrawable spriteTexture(IGuiHelper guiHelper, int v, int width, int height) {
        return guiHelper.drawableBuilder(TEXTURE, TEXTURE_WIDTH - SPRITE_SLOT_SIZE, v, width, height)
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
    }

    private Component requirementTooltip(String translationKey, String value) {
        return TextUtils.translate(Constants.TOOLTIP, translationKey, ChatFormatting.AQUA)
            .append(": ")
            .append(TextUtils.translate(translationKey, value.toLowerCase(), ChatFormatting.WHITE));
    }

    private boolean isWithinRequirement(double mX, double mY, int y) {
        return GameUtils.isWithinBounds(
            mX,
            mY,
            TEXTURE_WIDTH - 2 * SPRITE_SLOT_SIZE - 1,
            y,
            SPRITE_SLOT_SIZE,
            SPRITE_SLOT_SIZE
        );
    }

    @Override
    public RecipeType<AltarRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return TextUtils.translate(Constants.BLOCK, ALTAR);
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
        if (recipe.getBlockBelow() != null) {
            builder.addSlot(
                    RecipeIngredientRole.RENDER_ONLY,
                    CENTER_X - BLOCK_SLOT_SIZE / 2,
                    RENDER_Y - 3
                )
                .setCustomRenderer(AlmostTypes.BLOCK_REFERENCE, blockReferenceRenderer)
                .addIngredient(AlmostTypes.BLOCK_REFERENCE, recipe.getBlockBelow());
        }

        builder.addSlot(
                RecipeIngredientRole.INPUT,
                CENTER_X - ITEM_SLOT_SIZE / 2,
                RENDER_Y - 32
            )
            .setCustomRenderer(VanillaTypes.ITEM_STACK, catalystRenderer)
            .addIngredients(recipe.getCatalyst());

        var itemInputs = recipe.getInputs();
        var mobInputs = recipe.getSacrifices();
        var inputSlots = itemInputs.size() + mobInputs.size();

        for (var i = 0; i < inputSlots; i++) {
            var x = CENTER_X + (int) (Math.cos(i * 2 * Math.PI / inputSlots) * INPUT_RADIUS) - ITEM_SLOT_SIZE / 2;
            var y = RENDER_Y + (int) (Math.sin(i * 2 * Math.PI / inputSlots) * INPUT_RADIUS) - ITEM_SLOT_SIZE / 2;

            if (i < itemInputs.size()) {
                List<ItemStack> inputStacks = new ArrayList<>();
                for (var stack : itemInputs.get(i).ingredient().getItems()) {
                    stack.setCount(itemInputs.get(i).count());
                    inputStacks.add(stack);
                }
                builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStacks(inputStacks);
            } else {
                var mobInput = mobInputs.get(i - itemInputs.size());
                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addIngredient(AlmostTypes.MOB, new MobIngredient(mobInput.mob(), mobInput.count()));
            }
        }

        recipe.getOutputs().forEach((type, output, i) -> {
            var x = 2 + i * (ITEM_SLOT_SIZE - 1);
            var y = 130;

            if (type == OutputType.ITEM) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y).addItemStack((ItemStack) output.getOutput());
            } else if (type == OutputType.MOB) {
                var entityIngredient = new MobIngredient(
                    (EntityType<?>) output.getOutput(),
                    output.getCount(),
                    output.getData()
                );
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addIngredient(
                        AlmostTypes.MOB,
                        entityIngredient
                    );
                var egg = entityIngredient.getEgg();
                if (egg != null) {
                    builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(egg.getDefaultInstance());
                }
            }
        });
    }

    @Override
    public void draw(
        AltarRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY
    ) {
        stack.pushPose();
        {
            var altarY = RENDER_Y - BLOCK_SLOT_SIZE / 2f - (recipe.getBlockBelow() == null ? 0 : 4);
            stack.translate(CENTER_X - BLOCK_SLOT_SIZE / 2f, altarY, 0);
            altarRenderer.render(stack);
        }
        stack.popPose();

        GameUtils.renderText(
            stack,
            f("{}:", TextUtils.translateAsString(Constants.LABEL, Constants.OUTPUTS)),
            GameUtils.ANCHOR.BOTTOM_LEFT,
            2,
            128,
            1,
            0x36_A400
        );
        if (!recipe.getSacrifices().isEmpty()) {
            GameUtils.renderText(
                stack,
                f("{}:", TextUtils.translateAsString(Constants.LABEL, Constants.REGION)),
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
            spriteSlot.draw(stack, TEXTURE_WIDTH - 2 * SPRITE_SLOT_SIZE - 1, spriteOffset);
            if (recipe.getDayTime() == DAY_TIME.DAY) {
                daySprite.draw(stack, TEXTURE_WIDTH - 2 * SPRITE_SLOT_SIZE, spriteOffset + 1);
            } else if (recipe.getDayTime() == DAY_TIME.NIGHT) {
                nightSprite.draw(stack, TEXTURE_WIDTH - 2 * SPRITE_SLOT_SIZE, spriteOffset + 1);
            }
            spriteOffset += SPRITE_SLOT_SIZE + 1;
        }
        if (recipe.getWeather() != WEATHER.ANY) {
            spriteSlot.draw(stack, TEXTURE_WIDTH - 2 * SPRITE_SLOT_SIZE - 1, spriteOffset);
            switch (recipe.getWeather()) {
                case CLEAR -> sunSprite.draw(stack, TEXTURE_WIDTH - 2 * SPRITE_SLOT_SIZE, spriteOffset + 1);
                case RAIN -> rainSprite.draw(stack, TEXTURE_WIDTH - 2 * SPRITE_SLOT_SIZE, spriteOffset + 1);
                case THUNDER -> thunderSprite.draw(stack, TEXTURE_WIDTH - 2 * SPRITE_SLOT_SIZE, spriteOffset + 1);
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
            tooltip.add(TextUtils.translate(Constants.TOOLTIP, Constants.REGION, ChatFormatting.WHITE));
        }
        if (isWithinRequirement(mX, mY, 1)) {
            if (recipe.getDayTime() != DAY_TIME.ANY) {
                tooltip.add(requirementTooltip(Constants.DAY_TIME, recipe.getDayTime().name()));
            } else if (recipe.getWeather() != WEATHER.ANY) {
                tooltip.add(requirementTooltip(Constants.WEATHER, recipe.getWeather().name()));
            }
        }
        if (isWithinRequirement(mX, mY, SPRITE_SLOT_SIZE + 2)) {
            if (recipe.getWeather() != WEATHER.ANY) {
                tooltip.add(requirementTooltip(Constants.WEATHER, recipe.getWeather().name()));
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

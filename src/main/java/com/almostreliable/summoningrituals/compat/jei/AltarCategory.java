package com.almostreliable.summoningrituals.compat.jei;

import com.almostreliable.summoningrituals.Setup;
import com.almostreliable.summoningrituals.compat.jei.AlmostJEI.AlmostTypes;
import com.almostreliable.summoningrituals.compat.jei.ingredients.block.BlockStateRenderer;
import com.almostreliable.summoningrituals.compat.jei.ingredients.item.SizedItemRenderer;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
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
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.almostreliable.summoningrituals.Constants.ALTAR;
import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class AltarCategory implements IRecipeCategory<AltarRecipe> {

    public static final RecipeType<AltarRecipe> TYPE = new RecipeType<>(TextUtils.getRL(ALTAR), AltarRecipe.class);
    private static final ResourceLocation TEXTURE = TextUtils.getRL(f("textures/jei/{}.png", ALTAR));
    private static final int TEXTURE_WIDTH = 150;
    private static final int TEXTURE_HEIGHT = 157;
    private static final int CENTER_X = TEXTURE_WIDTH / 2;
    private static final int RENDER_Y = 50;
    private static final int RENDER_SIZE = 24;
    private static final int ITEM_RADIUS = 45;
    private static final int MOB_RADIUS = 65;

    private final IDrawable background;
    private final IDrawable icon;
    // private final IDrawable row;
    private final BlockStateRenderer blockStateRenderer;
    private final SizedItemRenderer sizedItemRenderer;

    public AltarCategory(IGuiHelper guiHelper) {
        // background = guiHelper.drawableBuilder(TEXTURE, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT)
        //     .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
        //     .build();
        background = guiHelper.createBlankDrawable(TEXTURE_WIDTH, TEXTURE_HEIGHT);
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Setup.ALTAR_ITEM.get()));
        // row = guiHelper.drawableBuilder(TEXTURE, 0, 0, 146, 18).setTextureSize(146, 38).build();
        blockStateRenderer = new BlockStateRenderer(RENDER_SIZE);
        sizedItemRenderer = new SizedItemRenderer(RENDER_SIZE);
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
        var yOffset = blockStateRenderer.getHeight() / 2;
        if (recipe.getBlockBelow() != null) {
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, CENTER_X - blockStateRenderer.getWidth() / 2, RENDER_Y + blockStateRenderer.getHeight())
                .setCustomRenderer(AlmostTypes.BLOCK_STATE, blockStateRenderer)
                .addIngredient(AlmostTypes.BLOCK_STATE, recipe.getBlockBelow().getDisplayState());
            yOffset = 0;
        }
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, CENTER_X - sizedItemRenderer.getWidth() / 2, RENDER_Y + yOffset)
            .setCustomRenderer(VanillaTypes.ITEM_STACK, sizedItemRenderer)
            .addItemStack(new ItemStack(Setup.ALTAR_ITEM.get()));
        builder.addSlot(RecipeIngredientRole.INPUT, CENTER_X - 8, RENDER_Y - 16 + yOffset)
            .addIngredients(recipe.getCatalyst());

        var inputs = recipe.getInputs();
        var inputSize = inputs.size();
        for (var i = 0; i < inputSize; i++) {
            var x = CENTER_X + (int) (Math.cos(i * 2 * Math.PI / inputSize) * ITEM_RADIUS) - 8;
            var y = RENDER_Y + (int) (Math.sin(i * 2 * Math.PI / inputSize) * ITEM_RADIUS) + 8;

            List<ItemStack> inputStacks = new ArrayList<>();
            for (var stack : inputs.get(i).ingredient().getItems()) {
                stack.setCount(inputs.get(i).count());
                inputStacks.add(stack);
            }

            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStacks(inputStacks);
        }
    }

    @Override
    public void draw(
        AltarRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY
    ) {
        // if (recipe.getBlockBelow() == null) return;
        // stack.pushPose();
        // stack.scale(16f, -16f, 1);
        // stack.mulPose(Vector3f.XP.rotationDegrees(25));
        // stack.mulPose(Vector3f.YP.rotationDegrees(45 + 180));
        // var bufferSource = mc.renderBuffers().bufferSource();
        // blockRenderer.renderSingleBlock(
        //     recipe.getBlockBelow().asBlockState(),
        //     stack,
        //     bufferSource,
        //     LightTexture.FULL_BRIGHT,
        //     OverlayTexture.NO_OVERLAY,
        //     EmptyModelData.INSTANCE
        // );
        // bufferSource.endBatch();
        // stack.popPose();
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

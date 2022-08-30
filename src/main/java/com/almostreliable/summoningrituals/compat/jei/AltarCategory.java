package com.almostreliable.summoningrituals.compat.jei;

import com.almostreliable.summoningrituals.Setup;
import com.almostreliable.summoningrituals.compat.jei.blockingredient.BlockRenderer;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static com.almostreliable.summoningrituals.Constants.ALTAR;
import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class AltarCategory implements IRecipeCategory<AltarRecipe> {

    public static final RecipeType<AltarRecipe> TYPE = new RecipeType<>(TextUtils.getRL(ALTAR), AltarRecipe.class);
    private static final ResourceLocation TEXTURE = TextUtils.getRL(f("textures/jei/{}.png", ALTAR));
    private static final int TEXTURE_WIDTH = 150;
    private static final int TEXTURE_HEIGHT = 157;

    private final Minecraft mc;
    private final BlockRenderDispatcher blockRenderer;
    private final IDrawable background;
    private final IDrawable icon;
    // private final IDrawable row;
    private final AltarRenderer altarRenderer;

    public AltarCategory(IGuiHelper guiHelper) {
        mc = Minecraft.getInstance();
        blockRenderer = mc.getBlockRenderer();
        background = guiHelper.drawableBuilder(TEXTURE, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
            .build();
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Setup.ALTAR_ITEM.get()));
        // row = guiHelper.drawableBuilder(TEXTURE, 0, 0, 146, 18).setTextureSize(146, 38).build();
        altarRenderer = new AltarRenderer(mc);
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
        // TODO: shift upwards if there is a block below
        if (recipe.getBlockBelow() != null) {
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, (TEXTURE_WIDTH - altarRenderer.getWidth()) / 2, 55)
                .setCustomRenderer(AlmostJEI.AlmostTypes.BLOCK, new BlockRenderer(32))
                .addIngredient(AlmostJEI.AlmostTypes.BLOCK, recipe.getBlockBelow().getDisplayState());
        }
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, (TEXTURE_WIDTH - altarRenderer.getWidth()) / 2, 35)
            .setCustomRenderer(VanillaTypes.ITEM_STACK, altarRenderer)
            .addItemStack(new ItemStack(Setup.ALTAR_ITEM.get()));
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

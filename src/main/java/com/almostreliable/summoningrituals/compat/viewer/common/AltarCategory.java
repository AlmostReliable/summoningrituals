package com.almostreliable.summoningrituals.compat.viewer.common;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.Registration;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.util.GameUtils;
import com.almostreliable.summoningrituals.util.MathUtils;
import com.almostreliable.summoningrituals.util.TextUtils;
import com.almostreliable.summoningrituals.util.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.almostreliable.summoningrituals.Constants.ALTAR;
import static com.almostreliable.summoningrituals.Constants.RECIPE_VIEWER;
import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class AltarCategory<I, R> {

    protected static final ResourceLocation TEXTURE = Utils.getRL(f("textures/{}/{}.png", RECIPE_VIEWER, ALTAR));
    protected static final int TEXTURE_WIDTH = 188;
    protected static final int TEXTURE_HEIGHT = 148;

    protected static final int ITEM_SLOT_SIZE = 18;
    protected static final int ITEM_SIZE = ITEM_SLOT_SIZE - 2;
    protected static final int BLOCK_SLOT_SIZE = 22;
    protected static final int BLOCK_SIZE = BLOCK_SLOT_SIZE - 2;
    protected static final int SPRITE_SLOT_SIZE = 16;

    protected static final int CENTER_X = (TEXTURE_WIDTH - SPRITE_SLOT_SIZE) / 2 + 1;
    protected static final int RENDER_Y = 64;

    private static final int INPUT_RADIUS = 47;

    protected final ItemStack altar;
    private final I logo;
    protected final R altarRenderer;
    protected final R catalystRenderer;

    protected final List<SpriteWidget> conditionSpriteWidgets = List.of(
        new SpriteWidget(0, altarRecipe -> altarRecipe.dayTime() == AltarRecipe.DAY_TIME.DAY),
        new SpriteWidget(1, altarRecipe -> altarRecipe.dayTime() == AltarRecipe.DAY_TIME.NIGHT),
        new SpriteWidget(2, altarRecipe -> altarRecipe.weather() == AltarRecipe.WEATHER.CLEAR),
        new SpriteWidget(3, altarRecipe -> altarRecipe.weather() == AltarRecipe.WEATHER.RAIN),
        new SpriteWidget(4, altarRecipe -> altarRecipe.weather() == AltarRecipe.WEATHER.THUNDER)
    );

    protected AltarCategory(I logo, R altarRenderer, R catalystRenderer) {
        altar = Registration.ALTAR_ITEM.get().getDefaultInstance();
        this.logo = logo;
        this.altarRenderer = altarRenderer;
        this.catalystRenderer = catalystRenderer;
    }

    public I getIcon() {
        return logo;
    }

    public Component getTitle() {
        return TextUtils.translate(Constants.BLOCK, ALTAR);
    }

    protected List<Component> getTooltip(
        AltarRecipe recipe, int x, int y, double mX, double mY
    ) {
        List<Component> tooltip = new ArrayList<>();
        if (!recipe.sacrifices().isEmpty() && MathUtils.isWithinBounds(mX, mY, x + 1, y + 1, 30, 20)) {
            tooltip.add(TextUtils.translate(Constants.TOOLTIP, Constants.REGION, ChatFormatting.WHITE));
        }
        if (isSpriteHovered(mX, mY, x, y + 1)) {
            if (recipe.dayTime() != AltarRecipe.DAY_TIME.ANY) {
                tooltip.add(createConditionTooltip(Constants.DAY_TIME, recipe.dayTime().name()));
            } else if (recipe.weather() != AltarRecipe.WEATHER.ANY) {
                tooltip.add(createConditionTooltip(Constants.WEATHER, recipe.weather().name()));
            }
        }
        if (isSpriteHovered(mX, mY, x, y + SPRITE_SLOT_SIZE + 2) && recipe.dayTime() != AltarRecipe.DAY_TIME.ANY) {
            if (recipe.weather() != AltarRecipe.WEATHER.ANY) {
                tooltip.add(createConditionTooltip(Constants.WEATHER, recipe.weather().name()));
            }
        }
        return tooltip;
    }

    private Component createConditionTooltip(String translationKey, String value) {
        return TextUtils.translate(Constants.TOOLTIP, translationKey, ChatFormatting.AQUA)
            .append(": ")
            .append(TextUtils.translate(translationKey, value.toLowerCase(), ChatFormatting.WHITE));
    }

    private boolean isSpriteHovered(double mX, double mY, int x, int y) {
        return MathUtils.isWithinBounds(
            mX,
            mY,
            x + TEXTURE_WIDTH - 2 * SPRITE_SLOT_SIZE - 1,
            y,
            SPRITE_SLOT_SIZE,
            SPRITE_SLOT_SIZE
        );
    }

    protected void drawLabel(GuiGraphics guiGraphics, String text, GameUtils.ANCHOR anchor, int x, int y, int color) {
        GameUtils.renderText(
            guiGraphics,
            text,
            anchor,
            x,
            y,
            1,
            color
        );
    }

    protected static void handleInputs(
        int offsetX, int offsetY, AltarRecipe recipe,
        ItemInputConsumer itemConsumer,
        MobInputConsumer mobConsumer
    ) {
        var itemInputs = recipe.inputs();
        var mobInputs = recipe.sacrifices();
        var inputSlots = itemInputs.size() + mobInputs.size();

        for (var i = 0; i < inputSlots; i++) {
            var x = offsetX + CENTER_X + (int) (Math.cos(i * 2 * Math.PI / inputSlots) * INPUT_RADIUS) - ITEM_SLOT_SIZE / 2;
            var y = offsetY + RENDER_Y + (int) (Math.sin(i * 2 * Math.PI / inputSlots) * INPUT_RADIUS) - ITEM_SLOT_SIZE / 2;

            if (i < itemInputs.size()) {
                List<ItemStack> inputStacks = new ArrayList<>();
                for (var stack : itemInputs.get(i).ingredient().getItems()) {
                    stack.setCount(itemInputs.get(i).count());
                    inputStacks.add(stack);
                }
                itemConsumer.accept(x, y, inputStacks);
            } else {
                var mobInput = mobInputs.get(i - itemInputs.size());
                var mobIngredient = new MobIngredient(mobInput.mob(), mobInput.count());
                var egg = mobIngredient.getEgg();
                mobConsumer.accept(x, y, mobIngredient, egg);
            }
        }
    }

    protected static void handleOutputs(
        int offsetX, int offsetY, AltarRecipe recipe,
        ItemOutputConsumer itemConsumer,
        MobOutputConsumer mobConsumer
    ) {
        recipe.outputs().forEach((type, output, i) -> {
            var x = offsetX + 2 + i * (ITEM_SLOT_SIZE - 1);
            var y = offsetY + 130;

            if (type == RecipeOutputs.OutputType.ITEM) {
                itemConsumer.accept(x, y, (ItemStack) output.getOutput());
            } else if (type == RecipeOutputs.OutputType.MOB) {
                var entityIngredient = new MobIngredient(
                    (EntityType<?>) output.getOutput(),
                    output.getCount(),
                    output.getData()
                );
                var egg = entityIngredient.getEgg();
                mobConsumer.accept(x, y, entityIngredient, egg);
            }
        });
    }

    @FunctionalInterface
    protected interface ItemInputConsumer {
        void accept(int x, int y, List<ItemStack> inputs);
    }

    @FunctionalInterface
    protected interface MobInputConsumer {
        void accept(int x, int y, MobIngredient mob, @Nullable SpawnEggItem egg);
    }

    @FunctionalInterface
    protected interface ItemOutputConsumer {
        void accept(int x, int y, ItemStack output);
    }

    @FunctionalInterface
    protected interface MobOutputConsumer {
        void accept(int x, int y, MobIngredient mob, @Nullable SpawnEggItem egg);
    }

    public static final class SpriteWidget implements Renderable, Predicate<AltarRecipe> {

        private static final int SPRITE_SIZE = SPRITE_SLOT_SIZE - 2;

        private final int offset;
        private final Predicate<AltarRecipe> renderPredicate;

        private SpriteWidget(int offset, Predicate<AltarRecipe> renderPredicate) {
            this.offset = offset;
            this.renderPredicate = renderPredicate;
        }

        public void render(GuiGraphics guiGraphics, int x, int y) {
            PoseStack stack = guiGraphics.pose();
            stack.pushPose();
            stack.translate(x + TEXTURE_WIDTH - 2 * SPRITE_SLOT_SIZE - 1, y, 0);
            render(guiGraphics, 0, 0, 0);
            stack.popPose();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mX, int mY, float partial) {
            guiGraphics.blit(
                TEXTURE,
                0,
                0,
                TEXTURE_WIDTH - SPRITE_SLOT_SIZE,
                0,
                SPRITE_SLOT_SIZE,
                SPRITE_SLOT_SIZE,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
            );
            guiGraphics.blit(
                TEXTURE,
                1,
                1,
                TEXTURE_WIDTH - SPRITE_SLOT_SIZE,
                SPRITE_SLOT_SIZE + offset * SPRITE_SIZE,
                SPRITE_SIZE,
                SPRITE_SIZE,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
            );
        }

        @Override
        public boolean test(AltarRecipe altarRecipe) {
            return renderPredicate.test(altarRecipe);
        }
    }
}

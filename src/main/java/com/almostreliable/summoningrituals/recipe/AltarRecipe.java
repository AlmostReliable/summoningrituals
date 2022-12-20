package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.Registration;
import com.almostreliable.summoningrituals.inventory.VanillaWrapper;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.recipe.component.IngredientStack;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs;
import com.almostreliable.summoningrituals.recipe.component.RecipeSacrifices;
import com.almostreliable.summoningrituals.util.GameUtils;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AltarRecipe implements Recipe<VanillaWrapper> {

    public static final Set<Ingredient> CATALYST_CACHE = new HashSet<>();

    @override
    @val final ResourceLocation id;
    @val final Ingredient catalyst;
    @val final RecipeOutputs outputs;
    @val final NonNullList<IngredientStack> inputs;
    @val final RecipeSacrifices sacrifices;
    @val final int recipeTime;
    @Nullable
    @val final BlockReference blockBelow;
    @val final DAY_TIME dayTime;
    @val final WEATHER weather;

    AltarRecipe(
        ResourceLocation id, Ingredient catalyst, RecipeOutputs outputs, NonNullList<IngredientStack> inputs,
        RecipeSacrifices sacrifices, int recipeTime, @Nullable BlockReference blockBelow, DAY_TIME dayTime,
        WEATHER weather
    ) {
        this.id = id;
        this.outputs = outputs;
        this.inputs = inputs;
        this.catalyst = catalyst;
        this.sacrifices = sacrifices;
        this.recipeTime = recipeTime;
        this.blockBelow = blockBelow;
        this.dayTime = dayTime;
        this.weather = weather;
    }

    @Override
    public boolean matches(VanillaWrapper inv, Level level) {
        if (inv.catalyst.isEmpty || !catalyst.test(inv.catalyst)) {
            return false;
        }

        var matchedItems = new Ingredient[inv.containerSize];
        List<Ingredient> matchedIngredients = new ArrayList<>();

        for (var slot = 0; slot < inv.items.size(); slot++) {
            var stack = inv.items.get(slot);
            if (!stack.isEmpty && matchedItems[slot] == null) {
                for (var input : inputs) {
                    if (!matchedIngredients.contains(input.ingredient()) && input.ingredient()
                        .test(stack) && stack.count >= input.count()) {
                        matchedItems[slot] = input.ingredient();
                        matchedIngredients.add(input.ingredient());
                    }
                }
            }
        }

        return matchedIngredients.size() == inputs.size();
    }

    @Override
    public ItemStack assemble(VanillaWrapper inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.ALTAR_RECIPE.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return Registration.ALTAR_RECIPE.type().get();
    }

    public enum WEATHER {
        ANY,
        CLEAR,
        RAIN,
        THUNDER;

        public boolean check(Level level, @Nullable ServerPlayer player) {
            var check = switch (this) {
                case ANY -> true;
                case CLEAR -> !level.isRaining && !level.isThundering;
                case RAIN -> level.isRaining;
                case THUNDER -> level.isThundering;
            };
            if (!check) {
                GameUtils.sendPlayerMessage(
                    player,
                    toString().toLowerCase(),
                    ChatFormatting.YELLOW
                );
            }
            return check;
        }
    }

    public enum DAY_TIME {
        ANY,
        DAY,
        NIGHT;

        public boolean check(Level level, @Nullable ServerPlayer player) {
            var check = switch (this) {
                case ANY -> true;
                case DAY -> level.isDay;
                case NIGHT -> level.isNight;
            };
            if (!check) {
                GameUtils.sendPlayerMessage(
                    player,
                    toString().toLowerCase(),
                    ChatFormatting.YELLOW
                );
            }
            return check;
        }
    }
}

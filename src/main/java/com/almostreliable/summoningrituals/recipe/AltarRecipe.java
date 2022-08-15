package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.Setup;
import com.almostreliable.summoningrituals.inventory.AltarInvWrapper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public class AltarRecipe implements Recipe<AltarInvWrapper> {

    private final ResourceLocation recipeId;
    private final RecipeOutput<?> output;
    private final NonNullList<IngredientStack> inputs;
    private final Ingredient catalyst;
    private final int recipeTime;
    private final int dayTime;
    @Nullable private final RecipeSacrifices sacrifices;
    @Nullable private final Block blockBelow;
    @Nullable private final String weather;

    public AltarRecipe(
        ResourceLocation recipeId, RecipeOutput<?> output, NonNullList<IngredientStack> inputs,
        Ingredient catalyst, int recipeTime, int dayTime, @Nullable RecipeSacrifices sacrifices,
        @Nullable Block blockBelow,
        @Nullable String weather
    ) {
        this.recipeId = recipeId;
        this.output = output;
        this.inputs = inputs;
        this.catalyst = catalyst;
        this.recipeTime = recipeTime;
        this.dayTime = dayTime;
        this.sacrifices = sacrifices;
        this.blockBelow = blockBelow;
        this.weather = weather;
    }

    @Override
    public boolean matches(AltarInvWrapper inv, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(AltarInvWrapper inv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return recipeId;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Setup.ALTAR_RECIPE.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return Setup.ALTAR_RECIPE.type().get();
    }

    public RecipeOutput<?> getOutput() {
        return output;
    }

    public NonNullList<IngredientStack> getInputs() {
        return inputs;
    }

    public Ingredient getCatalyst() {
        return catalyst;
    }

    public int getRecipeTime() {
        return recipeTime;
    }

    public int getDayTime() {
        return dayTime;
    }

    @Nullable
    public RecipeSacrifices getSacrifices() {
        return sacrifices;
    }

    @Nullable
    public Block getBlockBelow() {
        return blockBelow;
    }

    @Nullable
    public String getWeather() {
        return weather;
    }
}

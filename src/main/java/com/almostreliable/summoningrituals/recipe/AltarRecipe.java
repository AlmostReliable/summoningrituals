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
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AltarRecipe implements Recipe<AltarInvWrapper> {

    // TODO:
    // - add effects that are performed after recipe crafting (lighting, weather change)
    // - add a range for the daytime
    // - add presets for the crafting animation

    public static final Set<Ingredient> CATALYST_CACHE = new HashSet<>();

    private final ResourceLocation recipeId;
    private final RecipeOutput<?> output;
    private final NonNullList<IngredientStack> inputs;
    private final Ingredient catalyst;
    @Nullable private final RecipeSacrifices sacrifices;
    private final int recipeTime;
    @Nullable private final BlockState blockBelow;
    private final int dayTime;
    private final String weather;

    public AltarRecipe(
        ResourceLocation recipeId, RecipeOutput<?> output, NonNullList<IngredientStack> inputs, Ingredient catalyst,
        @Nullable RecipeSacrifices sacrifices, int recipeTime, @Nullable BlockState blockBelow, int dayTime,
        String weather
    ) {
        this.recipeId = recipeId;
        this.output = output;
        this.inputs = inputs;
        this.catalyst = catalyst;
        this.sacrifices = sacrifices;
        this.recipeTime = recipeTime;
        this.blockBelow = blockBelow;
        this.dayTime = dayTime;
        this.weather = weather;
    }

    @Override
    public boolean matches(AltarInvWrapper inv, Level level) {
        if (inv.getCatalyst().isEmpty() || inv.getInputs().isEmpty() || !catalyst.test(inv.getCatalyst())) {
            return false;
        }

        var matchedItems = new Ingredient[inv.getContainerSize()];
        List<Ingredient> matchedIngredients = new ArrayList<>();

        for (var slot = 0; slot < inv.getInputs().size(); slot++) {
            var stack = inv.getInputs().get(slot);
            if (!stack.isEmpty() && matchedItems[slot] == null) {
                for (var input : inputs) {
                    if (!matchedIngredients.contains(input.ingredient()) && input.ingredient()
                        .test(stack) && stack.getCount() >= input.count()) {
                        matchedItems[slot] = input.ingredient();
                        matchedIngredients.add(input.ingredient());
                    }
                }
            }
        }

        return matchedIngredients.size() == inputs.size();
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

    @Nullable
    public RecipeSacrifices getSacrifices() {
        return sacrifices;
    }

    public int getRecipeTime() {
        return recipeTime;
    }

    @Nullable
    public BlockState getBlockBelow() {
        return blockBelow;
    }

    public int getDayTime() {
        return dayTime;
    }

    public String getWeather() {
        return weather;
    }
}

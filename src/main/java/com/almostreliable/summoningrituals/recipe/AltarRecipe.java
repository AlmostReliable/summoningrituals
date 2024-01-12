package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.Registration;
import com.almostreliable.summoningrituals.inventory.VanillaWrapper;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.recipe.component.IngredientStack;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs;
import com.almostreliable.summoningrituals.recipe.component.RecipeSacrifices;
import com.almostreliable.summoningrituals.util.GameUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
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

    private final ResourceLocation id;
    private final Ingredient catalyst;
    private final RecipeOutputs outputs;
    private final NonNullList<IngredientStack> inputs;
    private final RecipeSacrifices sacrifices;
    private final int recipeTime;
    @Nullable private final BlockReference blockBelow;
    private final DAY_TIME dayTime;
    private final WEATHER weather;

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
        if (inv.getCatalyst().isEmpty() || !catalyst.test(inv.getCatalyst())) {
            return false;
        }

        var matchedItems = new Ingredient[inv.getContainerSize()];
        List<Ingredient> matchedIngredients = new ArrayList<>();

        for (var slot = 0; slot < inv.getItems().size(); slot++) {
            var stack = inv.getItems().get(slot);
            if (!stack.isEmpty() && matchedItems[slot] == null) {
                for (var input : inputs) {
                    if (!matchedIngredients.contains(input.ingredient()) &&
                        input.ingredient().test(stack) && stack.getCount() >= input.count()) {
                        matchedItems[slot] = input.ingredient();
                        matchedIngredients.add(input.ingredient());
                    }
                }
            }
        }

        return matchedIngredients.size() == inputs.size();
    }

    @Override
    public ItemStack assemble(VanillaWrapper inv, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
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
                case CLEAR -> !level.isRaining() && !level.isThundering();
                case RAIN -> level.isRaining();
                case THUNDER -> level.isThundering();
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
                case DAY -> level.isDay();
                case NIGHT -> level.isNight();
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

    public Ingredient getCatalyst() {
        return catalyst;
    }

    public RecipeOutputs getOutputs() {
        return outputs;
    }

    public NonNullList<IngredientStack> getInputs() {
        return inputs;
    }

    public RecipeSacrifices getSacrifices() {
        return sacrifices;
    }

    public int getRecipeTime() {
        return recipeTime;
    }

    @Nullable
    public BlockReference getBlockBelow() {
        return blockBelow;
    }

    public DAY_TIME getDayTime() {
        return dayTime;
    }

    public WEATHER getWeather() {
        return weather;
    }
}

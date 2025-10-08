package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.altar.inventory.AltarInventory;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.recipe.component.EntityOutput;
import com.almostreliable.summoningrituals.recipe.component.ItemOutput;
import com.almostreliable.summoningrituals.recipe.component.RecipeSacrifices;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record AltarRecipe(
    Ingredient catalyst, List<ItemOutput> itemOutputs, List<EntityOutput> entityOutputs, List<SizedIngredient> inputs,
    Optional<RecipeSacrifices> sacrifices, int recipeTime, List<LootItemCondition> summoningConditions
) implements Recipe<AltarInventory> {

    private static final Set<Item> CATALYSTS = new HashSet<>();
    private static final Set<Item> INPUTS = new HashSet<>();

    @Override
    public boolean matches(AltarInventory inv, Level level) {
        if (inv.getCatalyst().isEmpty() || !catalyst.test(inv.getCatalyst())) {
            return false;
        }

        var matchedItems = new Ingredient[inv.getSlots()];
        var matchedIngredients = new ArrayList<Ingredient>();

        for (var slot = 0; slot < inv.size(); slot++) {
            var stack = inv.getStackInSlot(slot);
            if (!stack.isEmpty() && matchedItems[slot] == null) {
                for (var input : inputs) {
                    if (
                        !matchedIngredients.contains(input.ingredient()) &&
                            input.ingredient().test(stack) &&
                            stack.getCount() >= input.count()
                    ) {
                        matchedItems[slot] = input.ingredient();
                        matchedIngredients.add(input.ingredient());
                    }
                }
            }
        }

        return matchedIngredients.size() == inputs.size();
    }

    @Override
    public ItemStack assemble(AltarInventory input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.ALTAR_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Registration.ALTAR_RECIPE_TYPE.get();
    }

    public static boolean isCatalyst(Item item) {
        return CATALYSTS.contains(item);
    }

    public static boolean isInput(Item item) {
        return INPUTS.contains(item);
    }

    public static void addCatalyst(Item item) {
        CATALYSTS.add(item);
    }

    public static void addInput(Item item) {
        INPUTS.add(item);
    }

    public static void clearCaches() {
        CATALYSTS.clear();
        INPUTS.clear();
    }
}

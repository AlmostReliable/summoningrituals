package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.recipe.input.EntityInputs;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;
import com.almostreliable.summoningrituals.recipe.output.ItemOutput;
import com.almostreliable.summoningrituals.recipe.output.RecipeOutput;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record AltarRecipe(
    Ingredient catalyst, List<ItemOutput> itemOutputs, List<EntityOutput> entityOutputs, List<SizedIngredient> itemInputs,
    EntityInputs entityInputs, List<LootItemCondition> startConditions, int ticks
) implements Recipe<RecipeInput> {

    private static final Set<Item> CATALYSTS = new HashSet<>();
    private static final Set<Item> INPUTS = new HashSet<>();

    @Override
    public boolean matches(RecipeInput inventory, Level level) {
        var matchedItems = new Ingredient[inventory.size()];
        var matchedIngredients = new ArrayList<Ingredient>();

        for (var slot = 0; slot < inventory.size(); slot++) {
            var stack = inventory.getItem(slot);
            if (!stack.isEmpty() && matchedItems[slot] == null) {
                for (var input : itemInputs) {
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

        return matchedIngredients.size() == itemInputs.size();
    }

    @Override
    public ItemStack assemble(RecipeInput inventory, HolderLookup.Provider registries) {
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

    public <E extends Entity, T extends RecipeOutput<E>> Collection<E> spawnOutputs(ServerLevel level, BlockPos origin, List<T> outputs) {
        var result = new ArrayList<E>();
        for (var output : outputs) {
            result.addAll(output.spawn(level, origin));
        }
        return ImmutableList.copyOf(result);
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

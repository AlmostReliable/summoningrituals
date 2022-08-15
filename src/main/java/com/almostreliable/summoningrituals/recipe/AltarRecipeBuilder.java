package com.almostreliable.summoningrituals.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.function.Consumer;

public class AltarRecipeBuilder {

    private final RecipeOutput<?> output;
    private final NonNullList<IngredientStack> inputs;
    @Nullable private Ingredient catalyst;
    private int recipeTime = -1;
    private final int dayTime = -1;
    @Nullable private RecipeSacrifices sacrifices;
    @Nullable private Block blockBelow;
    @Nullable private String weather;

    private AltarRecipeBuilder(RecipeOutput<?> output) {
        this.output = output;
        inputs = NonNullList.create();
    }

    public static AltarRecipeBuilder entity(ResourceLocation entity, int count) {
        return new AltarRecipeBuilder(RecipeOutput.of(entity, count));
    }

    public static AltarRecipeBuilder entity(ResourceLocation entity) {
        return new AltarRecipeBuilder(RecipeOutput.of(entity));
    }

    public static AltarRecipeBuilder item(ItemLike item, int count) {
        return new AltarRecipeBuilder(RecipeOutput.of(item, count));
    }

    public static AltarRecipeBuilder item(ItemLike item) {
        return new AltarRecipeBuilder(RecipeOutput.of(item));
    }

    public static AltarRecipeBuilder item(ItemStack item) {
        return new AltarRecipeBuilder(RecipeOutput.of(item));
    }

    private AltarRecipeBuilder input(IngredientStack... input) {
        Collections.addAll(inputs, input);
        return this;
    }

    public AltarRecipeBuilder input(Ingredient item, int count) {
        return input(new IngredientStack(item, count));
    }

    public AltarRecipeBuilder input(Ingredient item) {
        return input(item, 1);
    }

    public AltarRecipeBuilder input(ItemLike item, int count) {
        return input(Ingredient.of(item), count);
    }

    public AltarRecipeBuilder input(ItemLike item) {
        return input(item, 1);
    }

    public AltarRecipeBuilder input(TagKey<Item> item, int count) {
        return input(Ingredient.of(item), count);
    }

    public AltarRecipeBuilder input(TagKey<Item> item) {
        return input(item, 1);
    }

    public AltarRecipeBuilder catalyst(Ingredient catalyst) {
        this.catalyst = catalyst;
        return this;
    }

    public void build(Consumer<? super FinishedRecipe> consumer) {
        var namespace = output.getId().getNamespace();
        var outputId = output.getId().getPath();
    }
}

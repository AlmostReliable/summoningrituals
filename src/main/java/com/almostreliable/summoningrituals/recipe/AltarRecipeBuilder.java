package com.almostreliable.summoningrituals.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Collections;

public class AltarRecipeBuilder {

    private final RecipeOutput<?> output;
    private final NonNullList<IngredientStack> inputs;
    @Nullable private Ingredient catalyst;
    private int recipeTime = -1;
    private int dayTime = -1;
    @Nullable private RecipeSacrifices sacrifices;
    @Nullable private BlockState blockBelow;
    @Nullable private String weather;

    private AltarRecipeBuilder(RecipeOutput<?> output) {
        this.output = output;
        inputs = NonNullList.create();
        sacrifices = new RecipeSacrifices();
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

    public AltarRecipeBuilder recipeTime(int recipeTime) {
        this.recipeTime = recipeTime;
        return this;
    }

    public AltarRecipeBuilder dayTime(int dayTime) {
        this.dayTime = dayTime;
        return this;
    }

    public AltarRecipe build(ResourceLocation id) {
        // TODO: handle nullables and default values
        return new AltarRecipe(id, output, inputs, catalyst, recipeTime, dayTime, sacrifices, blockBelow, weather);
    }
}

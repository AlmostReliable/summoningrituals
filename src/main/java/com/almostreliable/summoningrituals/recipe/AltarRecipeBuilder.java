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
    @Nullable private RecipeSacrifices sacrifices;
    private int recipeTime = 100;
    @Nullable private BlockState blockBelow;
    private int dayTime = -1;
    private String weather = "any";

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
        if (this.catalyst != null) {
            throw new IllegalArgumentException("Catalyst has already been set");
        }
        this.catalyst = catalyst;
        return this;
    }

    public AltarRecipeBuilder recipeTime(int recipeTime) {
        this.recipeTime = recipeTime;
        return this;
    }

    public AltarRecipeBuilder dayTime(int dayTime) {
        if (this.dayTime != -1) {
            throw new IllegalArgumentException("dayTime has already been set");
        }
        this.dayTime = dayTime;
        return this;
    }

    public AltarRecipeBuilder sacrifices(RecipeSacrifices sacrifices) {
        this.sacrifices = sacrifices;
        return this;
    }

    public AltarRecipeBuilder blockBelow(BlockState blockBelow) {
        if (this.blockBelow != null) {
            throw new IllegalArgumentException("blockBelow has already been set");
        }
        this.blockBelow = blockBelow;
        return this;
    }

    public AltarRecipeBuilder weather(String weather) {
        if (!this.weather.equals("any")) {
            throw new IllegalArgumentException("weather has already been set");
        }
        this.weather = weather;
        return this;
    }

    public AltarRecipe build(ResourceLocation id) {
        // TODO: handle nullables and default values
        if (catalyst == null) {
            throw new IllegalArgumentException("Catalyst cannot be null");
        }

        return new AltarRecipe(id, output, inputs, catalyst, sacrifices, recipeTime, blockBelow, dayTime, weather);
    }

    private AltarRecipeBuilder input(IngredientStack... input) {
        Collections.addAll(inputs, input);
        return this;
    }
}

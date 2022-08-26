package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.recipe.AltarRecipe.DAY_TIME;
import com.almostreliable.summoningrituals.recipe.AltarRecipe.WEATHER;
import com.almostreliable.summoningrituals.recipe.RecipeOutputs.EntityOutputBuilder;
import com.almostreliable.summoningrituals.recipe.RecipeOutputs.ItemOutputBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class AltarRecipeBuilder {

    private final RecipeOutputs outputs;
    private final NonNullList<IngredientStack> inputs;
    private final Ingredient catalyst;
    private final int recipeTime;
    private RecipeSacrifices sacrifices;
    @Nullable private BlockReference blockBelow;
    private DAY_TIME dayTime = DAY_TIME.ANY;
    private WEATHER weather = WEATHER.ANY;

    private AltarRecipeBuilder(Ingredient catalyst, int recipeTime) {
        outputs = new RecipeOutputs();
        inputs = NonNullList.create();
        this.catalyst = catalyst;
        this.recipeTime = recipeTime;
        sacrifices = new RecipeSacrifices();
    }

    public static AltarRecipeBuilder builder(Ingredient catalyst, int recipeTime) {
        return new AltarRecipeBuilder(catalyst, recipeTime);
    }

    public static AltarRecipeBuilder builder(Ingredient catalyst) {
        return builder(catalyst, 100);
    }

    public AltarRecipeBuilder itemOutput(Consumer<ItemOutputBuilder> outputBuilder) {
        var builder = new ItemOutputBuilder();
        outputBuilder.accept(builder);
        outputs.add(builder.build());
        return this;
    }

    public AltarRecipeBuilder mobOutput(Consumer<EntityOutputBuilder> outputBuilder) {
        var builder = new EntityOutputBuilder();
        outputBuilder.accept(builder);
        outputs.add(builder.build());
        return this;
    }

    public AltarRecipeBuilder input(Ingredient item, int count) {
        inputs.add(new IngredientStack(item, count));
        return this;
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

    public AltarRecipeBuilder dayTime(String dayTime) {
        var dayTimeEnum = DAY_TIME.valueOf(dayTime.toUpperCase());
        if (dayTimeEnum != DAY_TIME.ANY) {
            throw new IllegalArgumentException("dayTime has already been set");
        }
        this.dayTime = dayTimeEnum;
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
        this.blockBelow = BlockReference.fromState(blockBelow);
        return this;
    }

    public AltarRecipeBuilder weather(String weather) {
        var weatherEnum = WEATHER.valueOf(weather.toUpperCase());
        if (weatherEnum != WEATHER.ANY) {
            throw new IllegalArgumentException("weather has already been set");
        }
        this.weather = weatherEnum;
        return this;
    }

    public AltarRecipe build(ResourceLocation id) {
        // TODO: handle nullables and default values

        return new AltarRecipe(id, outputs, inputs, catalyst, sacrifices, recipeTime, blockBelow, dayTime, weather);
    }
}

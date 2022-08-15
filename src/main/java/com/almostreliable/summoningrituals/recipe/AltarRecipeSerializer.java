package com.almostreliable.summoningrituals.recipe;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AltarRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<AltarRecipe> {

    private static final String BLOCK_BELOW = "block_below";
    private static final String CATALYST = "catalyst";
    private static final String DAY_TIME = "day_time";
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";
    private static final String RECIPE_TIME = "recipe_time";
    private static final String SACRIFICES = "sacrifices";
    private static final String WEATHER = "weather";

    @Override
    public AltarRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        var output = RecipeOutput.fromJson(json.getAsJsonObject(OUTPUT));

        NonNullList<IngredientStack> inputs = NonNullList.create();
        if (json.has(INPUT)) {
            var inputsJson = json.getAsJsonArray(INPUT);
            for (var inputJson : inputsJson) {
                inputs.add(IngredientStack.fromJson(inputJson.getAsJsonObject()));
            }
        }

        var catalyst = Ingredient.fromJson(json.getAsJsonObject(CATALYST));
        var recipeTime = GsonHelper.getAsInt(json, RECIPE_TIME, 100);
        var dayTime = GsonHelper.getAsInt(json, DAY_TIME, -1);

        RecipeSacrifices sacrifices = null;
        if (json.has(SACRIFICES)) {
            sacrifices = RecipeSacrifices.fromJson(json.getAsJsonObject(SACRIFICES));
        }

        BlockState blockBelow = null;
        if (json.has(BLOCK_BELOW)) {
            var blockString = GsonHelper.getAsString(json, BLOCK_BELOW);
            blockBelow = readBlockFromString(blockString);
        }

        var weather = GsonHelper.getAsString(json, WEATHER, "any");

        return new AltarRecipe(
            recipeId,
            output,
            inputs,
            catalyst,
            recipeTime,
            dayTime,
            sacrifices,
            blockBelow,
            weather
        );
    }

    @Nullable
    @Override
    public AltarRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        var output = RecipeOutput.fromNetwork(buffer);

        NonNullList<IngredientStack> inputs = NonNullList.create();
        var inputCount = buffer.readVarInt();
        for (var i = 0; i < inputCount; i++) {
            inputs.add(IngredientStack.fromNetwork(buffer));
        }

        var catalyst = Ingredient.fromNetwork(buffer);
        var recipeTime = buffer.readVarInt();
        var dayTime = buffer.readVarInt();

        RecipeSacrifices sacrifices = null;
        if (buffer.readBoolean()) {
            sacrifices = RecipeSacrifices.fromNetwork(buffer);
        }

        BlockState blockBelow = null;
        if (buffer.readBoolean()) {
            var blockString = buffer.readUtf();
            blockBelow = readBlockFromString(blockString);
        }

        var weather = buffer.readUtf();

        return new AltarRecipe(
            recipeId,
            output,
            inputs,
            catalyst,
            recipeTime,
            dayTime,
            sacrifices,
            blockBelow,
            weather
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AltarRecipe recipe) {
        recipe.getOutput().toNetwork(buffer);

        buffer.writeVarInt(recipe.getInputs().size());
        for (var input : recipe.getInputs()) {
            input.toNetwork(buffer);
        }

        recipe.getCatalyst().toNetwork(buffer);
        buffer.writeVarInt(recipe.getRecipeTime());
        buffer.writeVarInt(recipe.getDayTime());

        if (recipe.getSacrifices() != null) {
            buffer.writeBoolean(true);
            recipe.getSacrifices().toNetwork(buffer);
        } else {
            buffer.writeBoolean(false);
        }

        if (recipe.getBlockBelow() != null) {
            buffer.writeBoolean(true);
            buffer.writeUtf(BlockStateParser.serialize(recipe.getBlockBelow()));
        } else {
            buffer.writeBoolean(false);
        }

        buffer.writeUtf(recipe.getWeather());
    }

    private BlockState readBlockFromString(String blockString) {
        var reader = new StringReader(blockString);
        var parser = new BlockStateParser(reader, false);
        try {
            parser.parse(false);
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException("Invalid block state: " + blockString);
        }
        return Objects.requireNonNull(parser.getState());
    }
}

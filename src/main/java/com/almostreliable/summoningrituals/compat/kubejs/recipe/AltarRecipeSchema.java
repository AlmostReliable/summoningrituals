package com.almostreliable.summoningrituals.compat.kubejs.recipe;

import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.BlockPatternConditionComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.BlockPosComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.CommandOutputComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.EntityInputComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.EntityOutputComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.FakeEntityInputComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.ItemOutputComponent;
import com.almostreliable.summoningrituals.compat.kubejs.recipe.component.LootItemConditionComponent;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.condition.pattern.BlockPatternCondition;
import com.almostreliable.summoningrituals.recipe.input.EntityInput;
import com.almostreliable.summoningrituals.recipe.input.FakeEntityInput;
import com.almostreliable.summoningrituals.recipe.output.CommandOutput;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;
import com.almostreliable.summoningrituals.recipe.output.ItemOutput;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedIngredientComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.IntBounds;

import java.util.List;

public interface AltarRecipeSchema {

    RecipeKey<Ingredient> INITIATOR = IngredientComponent.INGREDIENT
        .key(Constants.INITIATOR, ComponentRole.INPUT)
        .noFunctions();
    RecipeKey<List<ItemOutput>> ITEM_OUTPUTS = ItemOutputComponent.TYPE
        .instance()
        .asList()
        .withBounds(IntBounds.OPTIONAL)
        .key(Constants.ITEM_OUTPUTS, ComponentRole.OUTPUT)
        .functionNames(List.of("itemOutputs", "itemOutput", "outputs", "output"))
        .optional(List.of())
        .exclude();
    RecipeKey<List<EntityOutput>> ENTITY_OUTPUTS = EntityOutputComponent.TYPE
        .instance()
        .asList()
        .withBounds(IntBounds.OPTIONAL)
        .key(Constants.ENTITY_OUTPUTS, ComponentRole.OUTPUT)
        .functionNames(List.of("entityOutputs", "entityOutput", "mobOutputs", "mobOutput"))
        .optional(List.of())
        .exclude();
    RecipeKey<CommandOutput> COMMANDS = CommandOutputComponent.TYPE
        .key(Constants.COMMANDS, ComponentRole.OUTPUT)
        .noFunctions()
        .optional(CommandOutput.EMPTY)
        .exclude();
    RecipeKey<List<ItemStack>> DISPLAY_OUTPUTS = ItemStackComponent.ITEM_STACK
        .instance()
        .asList()
        .withBounds(IntBounds.OPTIONAL)
        .key(Constants.DISPLAY_OUTPUTS, ComponentRole.OUTPUT)
        .functionNames(List.of("displayOutputs", "displayOutput"))
        .optional(List.of())
        .exclude();
    RecipeKey<List<SizedIngredient>> ITEM_INPUTS = SizedIngredientComponent.FLAT
        .instance()
        .asList()
        .withBounds(IntBounds.OPTIONAL)
        .key(Constants.ITEM_INPUTS, ComponentRole.INPUT)
        .functionNames(List.of("itemInputs", "itemInput", "inputs", "input"))
        .optional(List.of())
        .exclude();
    RecipeKey<List<EntityInput>> ENTITY_INPUTS = EntityInputComponent.TYPE
        .instance()
        .asList()
        .withBounds(IntBounds.OPTIONAL)
        .key(Constants.ENTITY_INPUTS, ComponentRole.INPUT)
        .functionNames(List.of("entityInputs", "entityInput", "mobInputs", "mobInput"))
        .optional(List.of())
        .exclude();
    RecipeKey<List<FakeEntityInput>> FAKE_ENTITY_INPUTS = FakeEntityInputComponent.TYPE
        .instance()
        .asList()
        .withBounds(IntBounds.OPTIONAL)
        .key(Constants.FAKE_ENTITY_INPUTS, ComponentRole.INPUT)
        .functionNames(List.of("fakeEntityInputs", "fakeEntityInput", "fakeMobInputs", "fakeMobInput"))
        .optional(List.of())
        .exclude();
    RecipeKey<BlockPatternCondition> BLOCK_PATTERN = BlockPatternConditionComponent.TYPE
        .key(Constants.BLOCK_PATTERN, ComponentRole.OTHER)
        .noFunctions()
        .defaultOptional()
        .exclude();
    RecipeKey<BlockPatternCondition> BLOCK_PATTERN_EXTENSION = BlockPatternConditionComponent.TYPE
        .key(Constants.BLOCK_PATTERN_EXTENSION, ComponentRole.OTHER)
        .noFunctions()
        .defaultOptional()
        .exclude();
    RecipeKey<BlockPos> ZONE = BlockPosComponent.TYPE
        .key(Constants.ZONE, ComponentRole.OTHER)
        .functionNames(List.of("entityInputZone", "mobInputZone", "inputZone", "sacrificeZone", "entityZone", "mobZone", "zone"))
        .optional(AltarRecipe.DEFAULT_ZONE)
        .exclude();
    RecipeKey<Integer> TICKS = NumberComponent.INT
        .key(Constants.TICKS, ComponentRole.OTHER)
        .functionNames(List.of("ticks", "time", "duration"))
        .optional(AltarRecipe.DEFAULT_TICKS)
        .exclude();
    RecipeKey<List<LootItemCondition>> CONDITIONS = LootItemConditionComponent.TYPE
        .instance()
        .asList()
        .withBounds(IntBounds.OPTIONAL)
        .key(Constants.CONDITIONS, ComponentRole.INPUT)
        .noFunctions()
        .optional(List.of())
        .exclude();

    RecipeSchema SCHEMA = new RecipeSchema(
        INITIATOR,
        ITEM_OUTPUTS,
        ENTITY_OUTPUTS,
        COMMANDS,
        DISPLAY_OUTPUTS,
        ITEM_INPUTS,
        ENTITY_INPUTS,
        FAKE_ENTITY_INPUTS,
        BLOCK_PATTERN,
        BLOCK_PATTERN_EXTENSION,
        ZONE,
        TICKS,
        CONDITIONS
    ).factory(AltarKubeRecipe.FACTORY);
}

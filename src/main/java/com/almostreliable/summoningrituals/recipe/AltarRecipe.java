package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.recipe.condition.pattern.BlockPatternCondition;
import com.almostreliable.summoningrituals.recipe.container.RecipeInputs;
import com.almostreliable.summoningrituals.recipe.container.RecipeOutputs;
import com.almostreliable.summoningrituals.recipe.input.EntityInput;
import com.almostreliable.summoningrituals.recipe.output.RecipeOutput;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

// TODO: add system that prints exact failed condition in game chat
public record AltarRecipe(
    Ingredient initiator, RecipeOutputs outputs, RecipeInputs inputs, List<LootItemCondition> conditions,
    Optional<BlockPatternCondition> blockPattern, Optional<BlockPatternCondition> blockPatternExtension,
    BlockPos zone, int ticks
) implements Recipe<RecipeInput> {

    public static final BlockPos DEFAULT_ZONE = new BlockPos(3, 2, 3);
    public static final int DEFAULT_TICKS = 40;
    private static final Set<Item> INITIATORS = Sets.newIdentityHashSet();
    private static final Set<Item> INPUTS = Sets.newIdentityHashSet();
    private static boolean CACHES_INITIALIZED;

    @Override
    public boolean matches(RecipeInput inventory, Level level) {
        var itemInputs = inputs.itemInputs();
        if (itemInputs.isEmpty()) return true;

        var size = inventory.size();
        var stacks = new ItemStack[size]; // reference only, do not modify
        var remainingCounts = new int[size];

        for (var slot = 0; slot < size; slot++) {
            var stack = inventory.getItem(slot);
            stacks[slot] = stack;
            remainingCounts[slot] = stack.getCount();
        }

        for (var input : itemInputs) {
            var remaining = input.count();
            for (var slot = 0; slot < size && remaining > 0; slot++) {
                if (remainingCounts[slot] <= 0) continue;

                var stack = stacks[slot];
                if (stack.isEmpty()) continue;
                if (input.ingredient().test(stack)) {
                    var taken = Math.min(remainingCounts[slot], remaining);
                    remainingCounts[slot] -= taken;
                    remaining -= taken;
                }
            }

            if (remaining > 0) return false;
        }

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

    @Nullable
    public List<Entity> getSacrifices(BlockPos pos, ResourceLocation recipeId, Function<AABB, List<Entity>> entityCollector) {
        var zoneRegion = constructRegion(pos);
        return inputs.getSacrifices(zoneRegion, recipeId, entityCollector);
    }

    // exposed for KubeJS
    public AABB constructRegion(BlockPos pos) {
        var startBounds = pos.offset(zone.multiply(-1));
        var endBounds = pos.offset(zone);
        return new AABB(
            Vec3.atLowerCornerOf(startBounds),
            Vec3.atLowerCornerOf(endBounds)
        );
    }

    public <E extends Entity, T extends RecipeOutput<E>> Collection<E> spawnOutputs(
        ServerLevel level, BlockPos origin, Function<RecipeOutputs, List<T>> factory
    ) {
        var result = new ArrayList<E>();
        for (var output : factory.apply(outputs)) {
            result.addAll(output.spawn(level, origin));
        }
        return ImmutableList.copyOf(result);
    }

    public void invokeCommands(ServerLevel level, @Nullable ServerPlayer player) {
        var commandOutput = outputs.commandOutput();
        if (commandOutput.isEmpty()) return;
        commandOutput.get().invoke(level, player);
    }

    //<editor-fold defaultstate="collapsed" desc="Default recipe stuff">
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
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Caching">
    public static boolean isInitiator(RecipeManager recipeManager, Item item) {
        if (!CACHES_INITIALIZED) initializeCaches(recipeManager);
        return INITIATORS.contains(item);
    }

    public static boolean isInput(RecipeManager recipeManager, Item item) {
        if (!CACHES_INITIALIZED) initializeCaches(recipeManager);
        return INPUTS.contains(item);
    }

    private static void initializeCaches(RecipeManager recipeManager) {
        var recipes = recipeManager.getAllRecipesFor(Registration.ALTAR_RECIPE_TYPE.get());

        for (var recipe : recipes) {
            var r = recipe.value();
            for (var initiator : r.initiator.getItems()) {
                INITIATORS.add(initiator.getItem());
            }
            for (var itemInput : r.inputs.itemInputs()) {
                for (var stack : itemInput.getItems()) {
                    INPUTS.add(stack.getItem());
                }
            }
        }

        CACHES_INITIALIZED = true;
    }

    public static void clearCaches() {
        INITIATORS.clear();
        INPUTS.clear();
        CACHES_INITIALIZED = false;
        EntityInput.DATA_VALIDATORS.clear();
        EntityInput.FAKE_DATA_VALIDATORS.clear();
    }
    //</editor-fold>
}

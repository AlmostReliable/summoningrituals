package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.recipe.input.BaseEntityInput;
import com.almostreliable.summoningrituals.recipe.input.EntityInput;
import com.almostreliable.summoningrituals.recipe.input.FakeEntityInput;
import com.almostreliable.summoningrituals.recipe.output.CommandOutput;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;
import com.almostreliable.summoningrituals.recipe.output.ItemOutput;
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
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import com.google.common.collect.ImmutableList;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public record AltarRecipe(
    Ingredient initiator, List<ItemOutput> itemOutputs, List<EntityOutput> entityOutputs, Optional<CommandOutput> commands,
    List<SizedIngredient> itemInputs, List<EntityInput> entityInputs, List<FakeEntityInput> fakeEntityInputs,
    List<LootItemCondition> startConditions, BlockPos zone, int ticks
) implements Recipe<RecipeInput> {

    public static final BlockPos DEFAULT_ZONE = new BlockPos(3, 2, 3);
    public static final int DEFAULT_TICKS = 40;
    private static final Set<Item> INITIATORS = new HashSet<>();
    private static final Set<Item> INPUTS = new HashSet<>();
    private static boolean CACHES_INITIALIZED;

    @Override
    public boolean matches(RecipeInput inventory, Level level) {
        if (itemInputs.isEmpty()) return true;

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

    @Nullable
    public List<Entity> getSacrifices(BlockPos pos, ResourceLocation recipeId, Function<AABB, List<Entity>> entityCollector) {
        if (entityInputs.isEmpty() && fakeEntityInputs.isEmpty()) return List.of();

        var region = constructRegion(pos);
        var remainingEntities = new ArrayList<>(entityCollector.apply(region));

        var entityInputSacrifices = consumeSacrifices(recipeId, entityInputs, e -> e.entityInfo().count(), remainingEntities);
        if (entityInputSacrifices == null) return null;
        var sacrifices = new ArrayList<>(entityInputSacrifices);

        var fakeEntityInputSacrifices = consumeSacrifices(recipeId, fakeEntityInputs, FakeEntityInput::count, remainingEntities);
        if (fakeEntityInputSacrifices == null) return null;
        sacrifices.addAll(fakeEntityInputSacrifices);

        return sacrifices;
    }

    @Nullable
    private static <T extends BaseEntityInput> List<Entity> consumeSacrifices(
        ResourceLocation recipeId, List<T> inputs, ToIntFunction<T> countSupplier, List<Entity> entities
    ) {
        var sacrifices = new ArrayList<Entity>();

        for (var i = 0; i < inputs.size(); i++) {
            var input = inputs.get(i);
            var requiredCount = countSupplier.applyAsInt(input);
            var inputIndex = i;

            var matches = entities.stream()
                .filter(e -> input.test(recipeId, inputIndex, e))
                .limit(requiredCount)
                .toList();

            if (matches.size() < requiredCount) return null;

            sacrifices.addAll(matches);
            entities.removeAll(matches);
        }

        return sacrifices;
    }

    public <E extends Entity, T extends RecipeOutput<E>> Collection<E> spawnOutputs(ServerLevel level, BlockPos origin, List<T> outputs) {
        var result = new ArrayList<E>();
        for (var output : outputs) {
            result.addAll(output.spawn(level, origin));
        }
        return ImmutableList.copyOf(result);
    }

    public void invokeCommands(ServerLevel level, @Nullable ServerPlayer player) {
        if (commands.isEmpty()) return;
        commands.get().invoke(level, player);
    }

    // exposed for KubeJS debugging
    @SuppressWarnings("WeakerAccess")
    public AABB constructRegion(BlockPos pos) {
        var startBounds = pos.offset(zone.multiply(-1));
        var endBounds = pos.offset(zone);
        return new AABB(
            Vec3.atLowerCornerOf(startBounds),
            Vec3.atLowerCornerOf(endBounds)
        );
    }

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
            for (var itemInput : r.itemInputs) {
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
    }
}

package com.almostreliable.summoningrituals.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class RecipeOutput<T> {

    private static final String COUNT = "count";
    private static final String ENTITY = "entity";
    private static final String ITEM = "item";

    protected final T entry;
    @Nullable private Vec3 offset;
    @Nullable private Vec3 spread;

    protected RecipeOutput(T entry) {
        this.entry = entry;
    }

    public static RecipeOutput<ResourceLocation> of(ResourceLocation entity) {
        return new EntityOutput(entity, 1);
    }

    public static RecipeOutput<ResourceLocation> of(ResourceLocation entity, int count) {
        return new EntityOutput(entity, count);
    }

    public static RecipeOutput<ItemStack> of(ItemStack item) {
        return new ItemOutput(item);
    }

    public static RecipeOutput<ItemStack> of(ItemLike item) {
        return new ItemOutput(new ItemStack(item, 1));
    }

    public static RecipeOutput<ItemStack> of(ItemLike item, int count) {
        return new ItemOutput(new ItemStack(item, count));
    }

    public static RecipeOutput<?> fromJson(JsonObject json) {
        if (json.has(ITEM)) {
            var stack = ShapedRecipe.itemStackFromJson(json);
            return new ItemOutput(stack);
        }
        if (json.has(ENTITY)) {
            var entity = new ResourceLocation(json.get(ENTITY).getAsString());
            var count = json.has(COUNT) ? json.get(COUNT).getAsInt() : 1;
            return new EntityOutput(entity, count);
        }
        throw new IllegalArgumentException("Invalid recipe output");
    }

    public T getEntry() {
        return entry;
    }

    public abstract int getCount();

    public abstract ResourceLocation getId();

    public void setOffset(Vec3 offset) {
        this.offset = offset;
    }

    public void setSpread(Vec3 spread) {
        this.spread = spread;
    }

    public static final class EntityOutput extends RecipeOutput<ResourceLocation> {

        private final int count;

        private EntityOutput(ResourceLocation entry, int count) {
            super(entry);
            this.count = count;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public ResourceLocation getId() {
            return entry;
        }
    }

    public static final class ItemOutput extends RecipeOutput<ItemStack> {

        private ItemOutput(ItemStack entry) {
            super(entry);
        }

        @Override
        public int getCount() {
            return entry.getCount();
        }

        @Override
        public ResourceLocation getId() {
            return Objects.requireNonNull(entry.getItem().getRegistryName());
        }
    }
}

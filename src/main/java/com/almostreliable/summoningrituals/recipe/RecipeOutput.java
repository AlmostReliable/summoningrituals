package com.almostreliable.summoningrituals.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public abstract class RecipeOutput<T>  {

    private static final String ITEM = "item";
    private static final String COUNT = "count";
    private static final String ENTITY = "entity";

    protected final T entry;
    @Nullable private Vec3 offset;
    @Nullable private Vec3 spread;

    protected RecipeOutput(T entry) {
        this.entry = entry;
    }

    public void setOffset(Vec3 offset) {
        this.offset = offset;
    }

    public void setSpread(Vec3 spread) {
        this.spread = spread;
    }

    public T getEntry() {
        return entry;
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

    public abstract int getCount();

    public static class EntityOutput extends RecipeOutput<ResourceLocation> {

        private final int count;

        public EntityOutput(ResourceLocation entry, int count) {
            super(entry);
            this.count = count;
        }

        @Override
        public int getCount() {
            return count;
        }
    }

    public static class ItemOutput extends RecipeOutput<ItemStack> {

        public ItemOutput(ItemStack entry) {
            super(entry);
        }

        @Override
        public int getCount() {
            return entry.getCount();
        }
    }
}

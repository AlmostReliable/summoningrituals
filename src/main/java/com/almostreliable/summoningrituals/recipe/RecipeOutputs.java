package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.util.GameUtils;
import com.almostreliable.summoningrituals.util.SerializeUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class RecipeOutputs {

    private final NonNullList<RecipeOutput<?>> outputs;

    public RecipeOutputs() {
        outputs = NonNullList.create();
    }

    private RecipeOutputs(NonNullList<RecipeOutput<?>> outputs) {
        this.outputs = outputs;
    }

    public static RecipeOutputs fromJson(JsonArray json) {
        NonNullList<RecipeOutput<?>> recipeOutputs = NonNullList.create();
        for (var output : json) {
            recipeOutputs.add(RecipeOutput.fromJson(output.getAsJsonObject()));
        }
        return new RecipeOutputs(recipeOutputs);
    }

    public static RecipeOutputs fromNetwork(FriendlyByteBuf buffer) {
        var length = buffer.readVarInt();
        NonNullList<RecipeOutput<?>> outputs = NonNullList.create();
        for (var i = 0; i < length; i++) {
            outputs.add(RecipeOutput.fromNetwork(buffer));
        }
        return new RecipeOutputs(outputs);
    }

    public void add(RecipeOutput<?> output) {
        outputs.add(output);
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeVarInt(outputs.size());
        for (var output : outputs) {
            output.toNetwork(buffer);
        }
    }

    public void handleRecipe(Level level, BlockPos origin) {
        for (var output : outputs) {
            output.handleRecipe(level, origin);
        }
    }

    private abstract static class RecipeOutput<T> {

        final T entry;
        private Vec3i offset;
        private Vec3i spread;

        private RecipeOutput(T entry) {
            this.entry = entry;
            offset = new Vec3i(0, 2, 0);
            spread = new Vec3i(1, 0, 1);
        }

        private static RecipeOutput<?> fromJson(JsonObject json) {
            RecipeOutput<?> output;
            if (json.has(Constants.ITEM)) {
                output = ItemOutput.fromJson(json);
            } else if (json.has(Constants.ENTITY)) {
                output = EntityOutput.fromJson(json);
            } else {
                throw new IllegalArgumentException("Invalid recipe output");
            }
            if (json.has(Constants.OFFSET)) {
                output.spread = SerializeUtils.vec3FromJson(json.getAsJsonObject(Constants.OFFSET));
            }
            if (json.has(Constants.SPREAD)) {
                output.spread = SerializeUtils.vec3FromJson(json.getAsJsonObject(Constants.SPREAD));
            }
            return output;
        }

        private static RecipeOutput<?> fromNetwork(FriendlyByteBuf buffer) {
            var i = buffer.readVarInt();
            RecipeOutput<?> output;
            if (i == 0) {
                output = ItemOutput.fromNetwork(buffer);
            } else if (i == 1) {
                output = EntityOutput.fromNetwork(buffer);
            } else {
                throw new IllegalArgumentException("Invalid recipe output");
            }
            output.offset = SerializeUtils.vec3FromNetwork(buffer);
            output.spread = SerializeUtils.vec3FromNetwork(buffer);
            return output;
        }

        void toNetwork(FriendlyByteBuf buffer) {
            SerializeUtils.vec3ToNetwork(buffer, offset);
            SerializeUtils.vec3ToNetwork(buffer, spread);
        }

        abstract void handleRecipe(Level level, BlockPos origin);

        abstract int getCount();
    }

    public static final class ItemOutput extends RecipeOutput<ItemStack> {

        private ItemOutput(ItemStack entry) {
            super(entry);
        }

        private static ItemOutput fromJson(JsonObject json) {
            var stack = ShapedRecipe.itemStackFromJson(json);
            return new ItemOutput(stack);
        }

        private static ItemOutput fromNetwork(FriendlyByteBuf buffer) {
            return new ItemOutput(buffer.readItem());
        }

        @Override
        void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeVarInt(0);
            buffer.writeItem(entry);
            super.toNetwork(buffer);
        }

        @Override
        int getCount() {
            return entry.getCount();
        }

        @Override
        void handleRecipe(Level level, BlockPos origin) {
            // TODO: add offset and spread to the position of the item
            GameUtils.dropItem(level, origin, entry, true);
        }
    }

    public static final class EntityOutput extends RecipeOutput<ResourceLocation> {

        private final int count;

        private EntityOutput(ResourceLocation entry, int count) {
            super(entry);
            this.count = count;
        }

        private static EntityOutput fromJson(JsonObject json) {
            var entity = new ResourceLocation(GsonHelper.getAsString(json, Constants.ENTITY));
            var count = GsonHelper.getAsInt(json, Constants.COUNT, 1);
            return new EntityOutput(entity, count);
        }

        private static EntityOutput fromNetwork(FriendlyByteBuf buffer) {
            var entity = new ResourceLocation(buffer.readUtf());
            var count = buffer.readVarInt();
            return new EntityOutput(entity, count);
        }

        @Override
        void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeVarInt(1);
            buffer.writeUtf(entry.toString());
            buffer.writeVarInt(getCount());
            super.toNetwork(buffer);
        }

        @Override
        int getCount() {
            return count;
        }

        @Override
        void handleRecipe(Level level, BlockPos origin) {
            // TODO: spawn entity in the world and use spread and offset
        }
    }

    private abstract static class RecipeOutputBuilder {

        private Vec3i offset;
        private Vec3i spread;

        private RecipeOutputBuilder() {
            offset = new Vec3i(0, 2, 0);
            spread = new Vec3i(1, 0, 1);
        }

        public abstract RecipeOutput<?> build();

        public RecipeOutputBuilder setOffset(Vec3i offset) {
            this.offset = offset;
            return this;
        }

        public RecipeOutputBuilder setSpread(Vec3i spread) {
            this.spread = spread;
            return this;
        }
    }

    public static class ItemOutputBuilder extends RecipeOutputBuilder {

        private ItemStack stack;

        public ItemOutputBuilder item(ItemStack item) {
            stack = item;
            return this;
        }

        public ItemOutputBuilder item(ItemLike item, int count) {
            item(new ItemStack(item, count));
            return this;
        }

        public ItemOutputBuilder item(ItemLike item) {
            return item(item, 1);
        }

        public ItemOutputBuilder count(int count) {
            stack.setCount(count);
            return this;
        }

        @Override
        public ItemOutput build() {
            return new ItemOutput(stack);
        }
    }

    public static class EntityOutputBuilder extends RecipeOutputBuilder {

        private ResourceLocation entity;
        private int count;

        public EntityOutputBuilder entity(ResourceLocation entity) {
            this.entity = entity;
            return this;
        }

        public EntityOutputBuilder count(int count) {
            this.count = count;
            return this;
        }

        @Override
        public EntityOutput build() {
            return new EntityOutput(entity, count);
        }
    }
}

package com.almostreliable.summoningrituals.recipe.component;

import com.google.gson.JsonArray;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;

public final class RecipeOutputs {

    private final NonNullList<RecipeOutput<?>> outputs;

    private RecipeOutputs(NonNullList<RecipeOutput<?>> outputs) {
        this.outputs = outputs;
    }

    public RecipeOutputs() {
        this(NonNullList.create());
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

    public JsonArray toJson() {
        JsonArray json = new JsonArray();
        for (var output : outputs) {
            json.add(output.toJson());
        }
        return json;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeVarInt(outputs.size());
        for (var output : outputs) {
            output.toNetwork(buffer);
        }
    }

    public void add(RecipeOutput<?> output) {
        outputs.add(output);
    }

    public void handleRecipe(ServerLevel level, BlockPos origin) {
        for (var output : outputs) {
            output.spawn(level, origin);
        }
    }

    public int size() {
        return outputs.size();
    }

    public void forEach(TriConsumer<OutputType, RecipeOutput<?>, Integer> consumer) {
        for (var i = 0; i < outputs.size(); i++) {
            var output = outputs.get(i);
            consumer.accept(output.type, output, i);
        }
    }

    public enum OutputType {
        ITEM, MOB
    }

    @SuppressWarnings("unused") // remapped by Rhino for Kube
    private abstract static class RecipeOutputBuilder {

        CompoundTag data;
        Vec3i offset;
        Vec3i spread;

        private RecipeOutputBuilder() {
            data = new CompoundTag();
            offset = new Vec3i(0, 2, 0);
            spread = new Vec3i(1, 0, 1);
        }

        public abstract RecipeOutput<?> build();

        public RecipeOutputBuilder data(CompoundTag data) {
            this.data = data;
            return this;
        }

        public RecipeOutputBuilder offset(int x, int y, int z) {
            this.offset = new Vec3i(x, y, z);
            return this;
        }

        public RecipeOutputBuilder spread(int x, int y, int z) {
            this.spread = new Vec3i(x, y, z);
            return this;
        }
    }

    @SuppressWarnings("unused") // remapped by Rhino for Kube
    public static class ItemOutputBuilder extends RecipeOutputBuilder {

        private ItemStack stack;

        public ItemOutputBuilder(ItemStack stack) {
            this.stack = stack;
        }

        public ItemOutputBuilder item(ItemStack item) {
            stack = item;
            return this;
        }

        @Override
        public ItemOutput build() {
            var output = new ItemOutput(stack);
            output.data = data;
            output.offset = offset;
            output.spread = spread;
            return output;
        }
    }

    @SuppressWarnings("unused") // remapped by Rhino for Kube
    public static class MobOutputBuilder extends RecipeOutputBuilder {

        private EntityType<?> mob;
        private int count;

        public MobOutputBuilder(EntityType<?> mob) {
            this.mob = mob;
            this.count = 1;
        }

        public MobOutputBuilder mob(EntityType<?> mob) {
            this.mob = mob;
            return this;
        }

        public MobOutputBuilder count(int count) {
            this.count = count;
            return this;
        }

        @Override
        public EntityOutput build() {
            var output = new EntityOutput(mob, count);
            output.data = data;
            output.offset = offset;
            output.spread = spread;
            return output;
        }
    }
}

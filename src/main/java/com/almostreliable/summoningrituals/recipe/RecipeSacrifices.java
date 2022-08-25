package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.Constants;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.function.Predicate;

public class RecipeSacrifices {

    private final int width;
    private final int height;
    private final NonNullList<Sacrifice> sacrifices;

    public RecipeSacrifices() {
        width = 2;
        height = 2;
        sacrifices = NonNullList.create();
    }

    private RecipeSacrifices(int width, int height, NonNullList<Sacrifice> sacrifices) {
        this.width = width;
        this.height = height;
        this.sacrifices = sacrifices;
    }

    public static RecipeSacrifices fromJson(JsonObject json) {
        var width = GsonHelper.getAsInt(json, Constants.WIDTH, 2);
        var height = GsonHelper.getAsInt(json, Constants.HEIGHT, 2);
        var entities = json.getAsJsonArray(Constants.ENTITIES);
        NonNullList<Sacrifice> sacrifices = NonNullList.create();
        for (var entity : entities) {
            sacrifices.add(Sacrifice.fromJson(entity.getAsJsonObject()));
        }
        return new RecipeSacrifices(width, height, sacrifices);
    }

    public static RecipeSacrifices fromNetwork(FriendlyByteBuf buffer) {
        var width = buffer.readVarInt();
        var height = buffer.readVarInt();
        var length = buffer.readVarInt();
        NonNullList<Sacrifice> sacrifices = NonNullList.create();
        for (var i = 0; i < length; i++) {
            sacrifices.add(Sacrifice.fromNetwork(buffer));
        }
        return new RecipeSacrifices(width, height, sacrifices);
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeVarInt(width);
        buffer.writeVarInt(height);
        buffer.writeVarInt(sacrifices.size());
        for (var sacrifice : sacrifices) {
            sacrifice.toNetwork(buffer);
        }
    }

    public void addSacrifice(ResourceLocation id, int count) {
        sacrifices.add(new Sacrifice(id, count));
    }

    public AABB getRegion(BlockPos pos) {
        return new AABB(pos.offset(-width, -height, -width), pos.offset(width, height, width));
    }

    public boolean test(Predicate<? super Sacrifice> predicate) {
        for (var sacrifice : sacrifices) {
            if (!predicate.test(sacrifice)) return false;
        }
        return true;
    }

    public record Sacrifice(ResourceLocation entity, int count) {

        private static Sacrifice fromJson(JsonObject json) {
            var entity = new ResourceLocation(GsonHelper.getAsString(json, Constants.ENTITY));
            var count = GsonHelper.getAsInt(json, Constants.COUNT, 1);
            return new Sacrifice(entity, count);
        }

        private static Sacrifice fromNetwork(FriendlyByteBuf buffer) {
            var entity = new ResourceLocation(buffer.readUtf());
            var count = buffer.readVarInt();
            return new Sacrifice(entity, count);
        }

        public boolean matches(Entity toCheck) {
            return entity.equals(toCheck.getType().getRegistryName());
        }

        private void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeUtf(entity.toString());
            buffer.writeVarInt(count);
        }
    }
}

package com.almostreliable.summoningrituals.util;

import com.almostreliable.summoningrituals.Constants;
import com.google.gson.JsonObject;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public final class SerializeUtils {

    private SerializeUtils() {}

    public static Vec3i vec3FromJson(JsonObject json) {
        var x = GsonHelper.getAsInt(json, "x", 0);
        var y = GsonHelper.getAsInt(json, "y", 0);
        var z = GsonHelper.getAsInt(json, "z", 0);
        return new Vec3i(x, y, z);
    }

    public static JsonObject vec3ToJson(Vec3i vec) {
        var json = new JsonObject();
        json.addProperty("x", vec.getX());
        json.addProperty("y", vec.getY());
        json.addProperty("z", vec.getZ());
        return json;
    }

    public static Vec3i vec3FromNetwork(FriendlyByteBuf buffer) {
        var x = buffer.readVarInt();
        var y = buffer.readVarInt();
        var z = buffer.readVarInt();
        return new Vec3i(x, y, z);
    }

    public static void vec3ToNetwork(FriendlyByteBuf buffer, Vec3i vec) {
        buffer.writeVarInt(vec.getX());
        buffer.writeVarInt(vec.getY());
        buffer.writeVarInt(vec.getZ());
    }

    public static JsonObject stackToJson(ItemStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("stack is empty");
        }
        var json = new JsonObject();
        var id = stack.getItem().getRegistryName();
        if (id == null) {
            throw new IllegalArgumentException("ItemStack has no registry name");
        }
        json.addProperty(Constants.ITEM, id.toString());
        json.addProperty(Constants.COUNT, stack.getCount());
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            json.addProperty(Constants.NBT, stack.getTag().toString());
        }
        return json;
    }

    public static EntityType<?> mobFromId(@Nullable ResourceLocation id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        var entity = ForgeRegistries.ENTITIES.getValue(id);
        if (entity == null) {
            throw new IllegalArgumentException("Entity " + id + " is not registered");
        }
        return entity;
    }

    public static EntityType<?> mobFromJson(JsonObject json) {
        var id = new ResourceLocation(GsonHelper.getAsString(json, Constants.MOB));
        return mobFromId(id);
    }

    public static EntityType<?> mobFromNetwork(FriendlyByteBuf buffer) {
        var id = new ResourceLocation(buffer.readUtf());
        return mobFromId(id);
    }
}

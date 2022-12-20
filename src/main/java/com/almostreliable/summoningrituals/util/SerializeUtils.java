package com.almostreliable.summoningrituals.util;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.platform.Platform;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
        json.addProperty("x", vec.x);
        json.addProperty("y", vec.y);
        json.addProperty("z", vec.z);
        return json;
    }

    public static Vec3i vec3FromNetwork(FriendlyByteBuf buffer) {
        var x = buffer.readVarInt();
        var y = buffer.readVarInt();
        var z = buffer.readVarInt();
        return new Vec3i(x, y, z);
    }

    public static void vec3ToNetwork(FriendlyByteBuf buffer, Vec3i vec) {
        buffer.writeVarInt(vec.x);
        buffer.writeVarInt(vec.y);
        buffer.writeVarInt(vec.z);
    }

    public static JsonObject stackToJson(ItemStack stack) {
        if (stack.isEmpty) {
            throw new IllegalArgumentException("stack is empty");
        }
        var json = new JsonObject();
        json.addProperty(Constants.ITEM, Platform.getId(stack.getItem()).toString());
        json.addProperty(Constants.COUNT, stack.count);
        if (stack.hasTag()) {
            assert stack.tag != null;
            json.addProperty(Constants.NBT, stack.tag.toString());
        }
        return json;
    }

    public static Block blockFromId(@Nullable ResourceLocation id) {
        return getFromRegistry(Registry.BLOCK, id);
    }

    public static EntityType<?> mobFromNetwork(FriendlyByteBuf buffer) {
        var id = new ResourceLocation(buffer.readUtf());
        return Platform.mobFromId(id);
    }

    public static Map<String, String> mapFromJson(JsonObject json) {
        return json.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.value.asString
            ));
    }

    public static JsonObject mapToJson(Map<String, String> map) {
        var json = new JsonObject();
        for (var entry : map.entrySet()) {
            json.addProperty(entry.key, entry.value);
        }
        return json;
    }

    public static Map<String, String> mapFromNetwork(FriendlyByteBuf buffer) {
        var map = new HashMap<String, String>();
        var size = buffer.readVarInt();
        for (var i = 0; i < size; i++) {
            map.put(buffer.readUtf(), buffer.readUtf());
        }
        return map;
    }

    public static void mapToNetwork(FriendlyByteBuf buffer, Map<String, String> map) {
        buffer.writeVarInt(map.size());
        for (var entry : map.entrySet()) {
            buffer.writeUtf(entry.key);
            buffer.writeUtf(entry.value);
        }
    }

    public static CompoundTag nbtFromString(String nbtString) {
        try {
            return TagParser.parseTag(nbtString);
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException("Invalid NBT string: " + nbtString, e);
        }
    }

    public static <T> T getFromRegistry(
        Registry<T> registry, @Nullable ResourceLocation id
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        return registry.get(id);
    }
}

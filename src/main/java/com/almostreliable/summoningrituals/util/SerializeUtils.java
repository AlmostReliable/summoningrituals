package com.almostreliable.summoningrituals.util;

import com.google.gson.JsonObject;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

public final class SerializeUtils {

    private SerializeUtils() {}

    public static Vec3i vec3FromJson(JsonObject json) {
        var x = GsonHelper.getAsInt(json, "x", 0);
        var y = GsonHelper.getAsInt(json, "y", 0);
        var z = GsonHelper.getAsInt(json, "z", 0);
        return new Vec3i(x, y, z);
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
}

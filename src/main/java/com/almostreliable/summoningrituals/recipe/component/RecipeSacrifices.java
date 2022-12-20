package com.almostreliable.summoningrituals.recipe.component;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.platform.Platform;
import com.almostreliable.summoningrituals.util.SerializeUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;

import java.util.function.Predicate;

public class RecipeSacrifices {

    private static final Vec3i DEFAULT_ZONE = new Vec3i(3, 2, 3);

    private final NonNullList<Sacrifice> sacrifices;
    private Vec3i region;

    public RecipeSacrifices() {
        sacrifices = NonNullList.create();
        region = DEFAULT_ZONE;
    }

    private RecipeSacrifices(NonNullList<Sacrifice> sacrifices, Vec3i region) {
        this.sacrifices = sacrifices;
        this.region = region;
    }

    public static RecipeSacrifices fromJson(JsonObject json) {
        var mobs = json.getAsJsonArray(Constants.MOBS);
        NonNullList<Sacrifice> sacrifices = NonNullList.create();
        for (var entity : mobs) {
            sacrifices.add(Sacrifice.fromJson(entity.asJsonObject));
        }
        var zone = json.has(Constants.REGION) ?
            SerializeUtils.vec3FromJson(json.getAsJsonObject(Constants.REGION))
            : DEFAULT_ZONE;
        return new RecipeSacrifices(sacrifices, zone);
    }

    public static RecipeSacrifices fromNetwork(FriendlyByteBuf buffer) {
        var length = buffer.readVarInt();
        NonNullList<Sacrifice> sacrifices = NonNullList.create();
        for (var i = 0; i < length; i++) {
            sacrifices.add(Sacrifice.fromNetwork(buffer));
        }
        var zone = SerializeUtils.vec3FromNetwork(buffer);
        return new RecipeSacrifices(sacrifices, zone);
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        var mobs = new JsonArray();
        for (var sacrifice : sacrifices) {
            mobs.add(sacrifice.toJson());
        }
        json.add(Constants.MOBS, mobs);
        json.add(Constants.REGION, SerializeUtils.vec3ToJson(region));
        return json;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeVarInt(sacrifices.size());
        for (var sacrifice : sacrifices) {
            sacrifice.toNetwork(buffer);
        }
        SerializeUtils.vec3ToNetwork(buffer, region);
    }

    public void add(EntityType<?> mob, int count) {
        sacrifices.add(new Sacrifice(mob, count));
    }

    public AABB getRegion(BlockPos pos) {
        return new AABB(pos.offset(region.multiply(-1)), pos.offset(region));
    }

    public boolean test(Predicate<? super Sacrifice> predicate) {
        return sacrifices.stream().allMatch(predicate);
    }

    public int size() {
        return sacrifices.size();
    }

    public Sacrifice get(int index) {
        return sacrifices.get(index);
    }

    public String getDisplayRegion() {
        return String.format("%dx%dx%d", region.x, region.y, region.z);
    }

    public boolean isEmpty() {
        return sacrifices.isEmpty();
    }

    public void setRegion(Vec3i region) {
        this.region = region;
    }

    public record Sacrifice(EntityType<?> mob, int count) implements Predicate<Entity> {

        private static Sacrifice fromJson(JsonObject json) {
            var mob = Platform.mobFromJson(json);
            var count = GsonHelper.getAsInt(json, Constants.COUNT, 1);
            return new Sacrifice(mob, count);
        }

        private static Sacrifice fromNetwork(FriendlyByteBuf buffer) {
            var mob = SerializeUtils.mobFromNetwork(buffer);
            var count = buffer.readVarInt();
            return new Sacrifice(mob, count);
        }

        @Override
        public boolean test(Entity entity) {
            return mob.equals(entity.getType());
        }

        private JsonElement toJson() {
            JsonObject json = new JsonObject();
            json.addProperty(Constants.MOB, Platform.getId(mob).toString());
            if (count > 1) {
                json.addProperty(Constants.COUNT, count);
            }
            return json;
        }

        private void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeUtf(Platform.getId(mob).toString());
            buffer.writeVarInt(count);
        }
    }
}

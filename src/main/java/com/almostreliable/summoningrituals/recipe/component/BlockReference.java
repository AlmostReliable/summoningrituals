package com.almostreliable.summoningrituals.recipe.component;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.platform.Platform;
import com.almostreliable.summoningrituals.util.SerializeUtils;
import com.almostreliable.summoningrituals.util.Utils;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public final class BlockReference implements Predicate<BlockState> {

    private final Block block;
    private final Map<String, String> properties;
    private final Map<Integer, Boolean> testCache;

    private BlockState displayState;

    private BlockReference(Block block, Map<String, String> properties) {
        this.block = block;
        this.properties = properties;
        this.testCache = new HashMap<>();
    }

    public static BlockReference fromJson(JsonObject json) {
        var blockId = new ResourceLocation(GsonHelper.getAsString(json, Constants.BLOCK));
        var block = SerializeUtils.blockFromId(blockId);
        Map<String, String> properties = new HashMap<>();
        if (json.has(Constants.PROPERTIES)) {
            properties = SerializeUtils.mapFromJson(json.getAsJsonObject(Constants.PROPERTIES));
        }
        return new BlockReference(block, properties);
    }

    public static BlockReference fromNetwork(FriendlyByteBuf buffer) {
        var blockId = buffer.readResourceLocation();
        var block = SerializeUtils.blockFromId(blockId);
        var properties = SerializeUtils.mapFromNetwork(buffer);
        return new BlockReference(block, properties);
    }

    public JsonObject toJson() {
        var json = new JsonObject();
        json.addProperty(Constants.BLOCK, Platform.getId(block).toString());
        if (!properties.isEmpty()) {
            json.add(Constants.PROPERTIES, SerializeUtils.mapToJson(properties));
        }
        return json;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(Platform.getId(block));
        SerializeUtils.mapToNetwork(buffer, properties);
    }

    @Override
    public boolean test(BlockState blockState) {
        var cached = testCache.get(Block.getId(blockState));
        if (cached != null) return cached;

        if (!block.equals(blockState.getBlock())) {
            testCache.put(Block.getId(blockState), false);
            return false;
        }

        var toCompareProps = blockState.getValues();
        for (var prop : properties.entrySet()) {
            if (toCompareProps.entrySet().stream().noneMatch(entry ->
                entry.getKey().getName().equalsIgnoreCase(prop.getKey()) &&
                    entry.getValue().toString().equalsIgnoreCase(prop.getValue()))) {
                testCache.put(Block.getId(blockState), false);
                return false;
            }
        }
        testCache.put(Block.getId(blockState), true);
        return true;
    }

    public BlockState getDisplayState() {
        if (displayState != null) return displayState;

        AtomicReference<BlockState> newState = new AtomicReference<>(block.defaultBlockState());
        for (Property<?> property : newState.get().getProperties()) {
            Object newValue = properties.get(property.getName());
            if (newValue == null) continue;
            try {
                newState.set(newState.get().setValue(property, Utils.cast(newValue)));
            } catch (Exception ignored) {
                property.getValue(newValue.toString())
                    .ifPresent(v -> newState.set(newState.get().setValue(property, Utils.cast(v))));
            }
        }
        displayState = newState.get();
        return displayState;
    }
}

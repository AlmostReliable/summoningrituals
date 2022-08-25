package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.Constants;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BlockReference implements Predicate<BlockState> {

    private final ResourceLocation block;
    private final Map<String, String> properties;

    public BlockReference(ResourceLocation block, Map<String, String> properties) {
        this.block = block;
        this.properties = properties;
    }

    public static BlockReference fromState(BlockState state) {
        var block = state.getBlock().getRegistryName();
        if (block == null) {
            throw new IllegalArgumentException("Block state block has no registry name");
        }
        var properties = new HashMap<String, String>();
        for (var entry : state.getValues().entrySet()) {
            properties.put(entry.getKey().getName().toLowerCase(), entry.getValue().toString().toLowerCase());
        }
        return new BlockReference(block, properties);
    }

    public static BlockReference fromJson(JsonObject json) {
        var blockId = GsonHelper.getAsString(json, Constants.BLOCK);
        var properties = json.getAsJsonObject(Constants.STATES).entrySet().stream()
            .collect(Collectors.toMap(
                Entry::getKey,
                entry -> entry.getValue().getAsString()
            ));
        return new BlockReference(new ResourceLocation(blockId), properties);
    }

    public static BlockReference fromNetwork(FriendlyByteBuf buffer) {
        var block = buffer.readResourceLocation();
        var size = buffer.readVarInt();
        var properties = new HashMap<String, String>();
        for (var i = 0; i < size; i++) {
            properties.put(buffer.readUtf(), buffer.readUtf());
        }
        return new BlockReference(block, properties);
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(block);
        buffer.writeVarInt(properties.size());
        for (var entry : properties.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeUtf(entry.getValue());
        }
    }

    @Override
    public boolean test(BlockState blockState) {
        if (!block.equals(blockState.getBlock().getRegistryName())) return false;
        var toCompareProps = blockState.getValues();
        for (var prop : properties.entrySet()) {
            if (toCompareProps.entrySet().stream().noneMatch(entry ->
                entry.getKey().getName().equalsIgnoreCase(prop.getKey()) &&
                    entry.getValue().toString().equalsIgnoreCase(prop.getValue()))) {
                return false;
            }
        }
        return true;
    }
}

package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.util.SerializeUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Predicate;

public class BlockReference implements Predicate<BlockState> {

    private final ResourceLocation block;
    private final Map<String, String> properties;

    public BlockReference(ResourceLocation block, Map<String, String> properties) {
        this.block = block;
        this.properties = properties;
    }

    public static BlockReference fromJson(JsonObject json) {
        var blockId = GsonHelper.getAsString(json, Constants.BLOCK);
        var properties = SerializeUtils.mapFromJson(json.getAsJsonObject(Constants.PROPERTIES));
        return new BlockReference(new ResourceLocation(blockId), properties);
    }

    public static BlockReference fromNetwork(FriendlyByteBuf buffer) {
        var block = buffer.readResourceLocation();
        var properties = SerializeUtils.mapFromNetwork(buffer);
        return new BlockReference(block, properties);
    }

    public JsonElement toJson() {
        var json = new JsonObject();
        json.addProperty(Constants.BLOCK, block.toString());
        json.add(Constants.PROPERTIES, SerializeUtils.mapToJson(properties));
        return json;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(block);
        SerializeUtils.mapToNetwork(buffer, properties);
    }

    // TODO: oh my, please get rid of this
    public BlockState asBlockState() {
        var blockString = block + "[" + properties.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .reduce((a, b) -> a + "," + b)
            .orElse("") + "]";

        var blockStateParser = new BlockStateParser(new StringReader(blockString), false);
        try {
            blockStateParser.parse(false);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        if (blockStateParser.getState() == null) {
            throw new IllegalArgumentException();
        }
        return blockStateParser.getState();
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

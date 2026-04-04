package com.almostreliable.summoningrituals.compat.kubejs.builder;

import com.almostreliable.summoningrituals.recipe.condition.custom.BlockPatternCheck;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockPatternBuilder {

    private static final int MAX_PATTERN_SIZE = 32;

    private final Map<BlockPos, BlockPredicate> definitions = new HashMap<>();

    public BlockPatternBuilder block(Context ctx, BlockPos offset, Block block) {
        var predicate = BlockPredicate.Builder.block().of(block).build();
        return addDefinition(ctx, offset, predicate);
    }

    public BlockPatternBuilder block(Context ctx, BlockPos offset, Block block, JsonObject blockState) {
        var propertyBuilder = createPropertyBuilder(ctx, block, blockState);
        var predicate = BlockPredicate.Builder.block().of(block).setProperties(propertyBuilder).build();
        return addDefinition(ctx, offset, predicate);
    }

    public BlockPatternBuilder tag(Context ctx, BlockPos offset, TagKey<Block> blockTag) {
        var predicate = BlockPredicate.Builder.block().of(blockTag).build();
        return addDefinition(ctx, offset, predicate);
    }

    public BlockPatternBuilder tag(Context ctx, BlockPos offset, TagKey<Block> blockTag, JsonObject blockState) {
        var blocks = BuiltInRegistries.BLOCK.getTagOrEmpty(blockTag);
        if (!blocks.iterator().hasNext()) {
            throwException(ctx, "tag '" + blockTag + "' is empty");
        }

        var propertyBuilder = createPropertyBuilder(ctx, blocks.iterator().next().value(), blockState);
        var predicate = BlockPredicate.Builder.block().of(blockTag).setProperties(propertyBuilder).build();
        return addDefinition(ctx, offset, predicate);
    }

    private BlockPatternBuilder addDefinition(Context ctx, BlockPos offset, BlockPredicate predicate) {
        if (definitions.containsKey(offset)) {
            throwException(ctx, "position '" + offset + "' already defined in pattern");
        }

        if (Math.abs(offset.getX()) > MAX_PATTERN_SIZE || Math.abs(offset.getY()) > MAX_PATTERN_SIZE ||
            Math.abs(offset.getZ()) > MAX_PATTERN_SIZE) {
            throwException(ctx, "pattern size exceeds maximum allowed size of " + MAX_PATTERN_SIZE + " blocks");
        }

        definitions.put(offset, predicate);
        return this;
    }

    private StatePropertiesPredicate.Builder createPropertyBuilder(Context ctx, Block block, JsonObject blockState) {
        var definition = block.getStateDefinition();
        var propertyBuilder = StatePropertiesPredicate.Builder.properties();

        for (var entry : blockState.entrySet()) {
            var property = definition.getProperty(entry.getKey());
            if (property == null) {
                throwException(ctx, "unknown block property: " + entry.getKey());
            }
            propertyBuilder.hasProperty(property, entry.getValue().getAsString());
        }

        return propertyBuilder;
    }

    @HideFromJS
    public List<BlockPatternCheck.PatternEntry> build(Context ctx) {
        if (definitions.isEmpty()) {
            throwException(ctx, "block pattern cannot be empty");
        }

        var result = new ArrayList<BlockPatternCheck.PatternEntry>();
        for (var entry : definitions.entrySet()) {
            result.add(new BlockPatternCheck.PatternEntry(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private void throwException(Context ctx, String message) throws KubeRuntimeException {
        throw new KubeRuntimeException(message).source(SourceLine.of(ctx));
    }
}

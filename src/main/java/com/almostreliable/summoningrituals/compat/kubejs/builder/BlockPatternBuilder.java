package com.almostreliable.summoningrituals.compat.kubejs.builder;

import com.almostreliable.summoningrituals.recipe.condition.custom.BlockPatternCheck.PatternEntry;

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

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlockPatternBuilder {

    private static final int MAX_PATTERN_SIZE = 32;

    private final Map<BlockPos, PatternDefinition> definitions = new HashMap<>();

    public BlockPatternBuilder block(Context ctx, BlockPos offset, Block block) {
        var predicate = BlockPredicate.Builder.block().of(block).build();
        return addDefinition(ctx, offset, predicate, null);
    }

    public BlockPatternBuilder block(Context ctx, BlockPos offset, Block block, String queryId) {
        var predicate = BlockPredicate.Builder.block().of(block).build();
        return addDefinition(ctx, offset, predicate, queryId);
    }

    public BlockPatternBuilder block(Context ctx, BlockPos offset, Block block, JsonObject blockState) {
        var propertyBuilder = createPropertyBuilder(ctx, block, blockState);
        var predicate = BlockPredicate.Builder.block().of(block).setProperties(propertyBuilder).build();
        return addDefinition(ctx, offset, predicate, null);
    }

    public BlockPatternBuilder block(Context ctx, BlockPos offset, Block block, JsonObject blockState, String queryId) {
        var propertyBuilder = createPropertyBuilder(ctx, block, blockState);
        var predicate = BlockPredicate.Builder.block().of(block).setProperties(propertyBuilder).build();
        return addDefinition(ctx, offset, predicate, queryId);
    }

    public BlockPatternBuilder tag(Context ctx, BlockPos offset, TagKey<Block> blockTag) {
        var predicate = BlockPredicate.Builder.block().of(blockTag).build();
        return addDefinition(ctx, offset, predicate, null);
    }

    public BlockPatternBuilder tag(Context ctx, BlockPos offset, TagKey<Block> blockTag, String queryId) {
        var predicate = BlockPredicate.Builder.block().of(blockTag).build();
        return addDefinition(ctx, offset, predicate, queryId);
    }

    public BlockPatternBuilder tag(Context ctx, BlockPos offset, TagKey<Block> blockTag, JsonObject blockState) {
        var blocks = BuiltInRegistries.BLOCK.getTagOrEmpty(blockTag);
        if (!blocks.iterator().hasNext()) {
            throwException(ctx, "tag '" + blockTag + "' is empty");
        }

        var propertyBuilder = createPropertyBuilder(ctx, blocks.iterator().next().value(), blockState);
        var predicate = BlockPredicate.Builder.block().of(blockTag).setProperties(propertyBuilder).build();
        return addDefinition(ctx, offset, predicate, null);
    }

    public BlockPatternBuilder tag(Context ctx, BlockPos offset, TagKey<Block> blockTag, JsonObject blockState, String queryId) {
        var blocks = BuiltInRegistries.BLOCK.getTagOrEmpty(blockTag);
        if (!blocks.iterator().hasNext()) {
            throwException(ctx, "tag '" + blockTag + "' is empty");
        }

        var propertyBuilder = createPropertyBuilder(ctx, blocks.iterator().next().value(), blockState);
        var predicate = BlockPredicate.Builder.block().of(blockTag).setProperties(propertyBuilder).build();
        return addDefinition(ctx, offset, predicate, queryId);
    }

    private BlockPatternBuilder addDefinition(Context ctx, BlockPos offset, BlockPredicate predicate, @Nullable String queryId) {
        if (definitions.containsKey(offset)) {
            throwException(ctx, "position '" + offset + "' already defined in pattern");
        }

        if (Math.abs(offset.getX()) > MAX_PATTERN_SIZE || Math.abs(offset.getY()) > MAX_PATTERN_SIZE ||
            Math.abs(offset.getZ()) > MAX_PATTERN_SIZE) {
            throwException(ctx, "pattern size exceeds maximum allowed size of " + MAX_PATTERN_SIZE + " blocks");
        }

        definitions.put(offset, new PatternDefinition(predicate, Optional.ofNullable(queryId)));
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
    public List<PatternEntry> build(Context ctx) {
        if (definitions.isEmpty()) {
            throwException(ctx, "block pattern cannot be empty");
        }

        var result = new ArrayList<PatternEntry>();
        for (var entry : definitions.entrySet()) {
            var definition = entry.getValue();
            result.add(new PatternEntry(entry.getKey(), definition.predicate, definition.queryId));
        }
        return result;
    }

    private void throwException(Context ctx, String message) throws KubeRuntimeException {
        throw new KubeRuntimeException(message).source(SourceLine.of(ctx));
    }

    private record PatternDefinition(BlockPredicate predicate, Optional<String> queryId) {}
}

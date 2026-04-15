package com.almostreliable.summoningrituals.compat.kubejs.builder;

import com.almostreliable.summoningrituals.recipe.condition.pattern.BlockPatternCondition;
import com.almostreliable.summoningrituals.recipe.condition.pattern.BlockPatternCondition.PatternEntry;
import com.almostreliable.summoningrituals.recipe.condition.pattern.ResolvableBlockPredicate;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BlockPatternConditionBuilder {

    public static final int MAX_PATTERN_RADIUS = 32;

    private @Nullable Component name;
    private @Nullable List<Component> tooltip;
    private final Set<PatternEntry> entries = new HashSet<>();

    @ReturnsSelf
    public BlockPatternConditionBuilder name(Component name) {
        this.name = name;
        return this;
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder tooltip(List<Component> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder block(Context ctx, BlockPos offset, Block block) {
        return addBlockEntry(ctx, offset, block, null, null);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder block(Context ctx, BlockPos offset, Block block, String queryId) {
        return addBlockEntry(ctx, offset, block, null, queryId);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder block(Context ctx, BlockPos offset, Block block, JsonObject blockState) {
        return addBlockEntry(ctx, offset, block, blockState, null);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder block(Context ctx, BlockPos offset, Block block, JsonObject blockState, String queryId) {
        return addBlockEntry(ctx, offset, block, blockState, queryId);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder tag(Context ctx, BlockPos offset, TagKey<Block> blockTag) {
        return addTagEntry(ctx, offset, blockTag, null, null);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder tag(Context ctx, BlockPos offset, TagKey<Block> blockTag, String queryId) {
        return addTagEntry(ctx, offset, blockTag, null, queryId);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder tag(Context ctx, BlockPos offset, TagKey<Block> blockTag, JsonObject blockState) {
        return addTagEntry(ctx, offset, blockTag, blockState, null);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder tag(Context ctx, BlockPos offset, TagKey<Block> blockTag, JsonObject blockState, String queryId) {
        return addTagEntry(ctx, offset, blockTag, blockState, queryId);
    }

    private BlockPatternConditionBuilder addBlockEntry(
        Context ctx, BlockPos offset, Block block, @Nullable JsonObject blockState, @Nullable String queryId
    ) {
        var predicateBuilder = ResolvableBlockPredicate.builder(block);
        return addEntry(ctx, offset, predicateBuilder, blockState, queryId);
    }

    private BlockPatternConditionBuilder addTagEntry(
        Context ctx, BlockPos offset, TagKey<Block> blockTag, @Nullable JsonObject blockState, @Nullable String queryId
    ) {
        var predicateBuilder = ResolvableBlockPredicate.builder(blockTag);
        return addEntry(ctx, offset, predicateBuilder, blockState, queryId);
    }

    private BlockPatternConditionBuilder addEntry(
        Context ctx, BlockPos offset, ResolvableBlockPredicate.Builder predicateBuilder,
        @Nullable JsonObject blockState, @Nullable String queryId
    ) {
        if (Math.abs(offset.getX()) > MAX_PATTERN_RADIUS || Math.abs(offset.getY()) > MAX_PATTERN_RADIUS ||
            Math.abs(offset.getZ()) > MAX_PATTERN_RADIUS) {
            throwException(ctx, "pattern size exceeds maximum allowed radius of " + MAX_PATTERN_RADIUS + " blocks");
        }

        if (blockState != null && !blockState.isEmpty()) {
            var blockStates = predicateBuilder.build().getBlockStates();
            if (!blockStates.isEmpty()) {
                var propertyBuilder = createPropertyBuilder(ctx, blockStates.getFirst().getBlock(), blockState);
                predicateBuilder.setProperties(propertyBuilder);
            }
        }

        var patternEntry = new PatternEntry(offset, predicateBuilder.build(), Optional.ofNullable(queryId));
        if (entries.contains(patternEntry)) {
            throwException(ctx, "position '" + offset + "' already defined in pattern");
        }

        entries.add(patternEntry);
        return this;
    }

    private StatePropertiesPredicate.Builder createPropertyBuilder(Context ctx, Block block, JsonObject blockState) {
        var definition = block.getStateDefinition();
        var propertyBuilder = StatePropertiesPredicate.Builder.properties();

        for (var entry : blockState.entrySet()) {
            var property = definition.getProperty(entry.getKey());
            if (property == null) {
                var blockId = BuiltInRegistries.BLOCK.wrapAsHolder(block).getRegisteredName();
                throwException(ctx, "unknown block property '" + entry.getKey() + "' for block '" + blockId + "'");
            }
            propertyBuilder.hasProperty(property, entry.getValue().getAsString());
        }

        return propertyBuilder;
    }

    @HideFromJS
    public BlockPatternCondition build(Context ctx) {
        if (entries.isEmpty()) {
            throwException(ctx, "block pattern cannot be empty");
        }

        return new BlockPatternCondition(
            List.copyOf(entries),
            Optional.ofNullable(name),
            tooltip == null ? List.of() : tooltip
        );
    }

    private void throwException(Context ctx, String message) throws KubeRuntimeException {
        throw new KubeRuntimeException(message).source(SourceLine.of(ctx));
    }
}

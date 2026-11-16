package com.almostreliable.summoningrituals.compat.kubegroovy.builder;

import com.almostreliable.summoningrituals.recipe.condition.pattern.BlockPatternCondition;
import com.almostreliable.summoningrituals.recipe.condition.pattern.BlockPatternCondition.PatternEntry;
import com.almostreliable.summoningrituals.recipe.condition.pattern.ResolvableBlockPredicate;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import com.google.gson.JsonObject;
import dev.uncandango.kubegroovy.error.KubeRuntimeException;
import dev.uncandango.kubegroovy.script.SourceLine;
import dev.uncandango.kubegroovy.util.RegistryAccessContainer;
import dev.uncandango.kubegroovy.script.annotations.HideFromGY;
import dev.uncandango.kubegroovy.script.annotations.ReturnsSelf;

import org.jspecify.annotations.Nullable;

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
    public BlockPatternConditionBuilder block(BlockPos offset, Block block) {
        return addBlockEntry(offset, block, null, null);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder block(BlockPos offset, Block block, JsonObject blockState) {
        return addBlockEntry(offset, block, blockState, null);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder queryableBlock(BlockPos offset, Block block, String query) {
        return addBlockEntry(offset, block, null, query);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder queryableBlock(BlockPos offset, Block block, JsonObject blockState, String query) {
        return addBlockEntry(offset, block, blockState, query);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder tag(BlockPos offset, TagKey<Block> blockTag) {
        return addTagEntry(offset, blockTag, null, null);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder tag(BlockPos offset, TagKey<Block> blockTag, JsonObject blockState) {
        return addTagEntry(offset, blockTag, blockState, null);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder queryableTag(BlockPos offset, TagKey<Block> blockTag, String query) {
        return addTagEntry(offset, blockTag, null, query);
    }

    @ReturnsSelf
    public BlockPatternConditionBuilder queryableTag(
        BlockPos offset, TagKey<Block> blockTag, JsonObject blockState, String query
    ) {
        return addTagEntry(offset, blockTag, blockState, query);
    }

    private BlockPatternConditionBuilder addBlockEntry(
        BlockPos offset, Block block, @Nullable JsonObject blockState, @Nullable String query
    ) {
        var predicateBuilder = ResolvableBlockPredicate.builder(block);
        return addEntry(offset, predicateBuilder, blockState, query);
    }

    private BlockPatternConditionBuilder addTagEntry(
        BlockPos offset, TagKey<Block> blockTag, @Nullable JsonObject blockState, @Nullable String query
    ) {
        var blocks = RegistryAccessContainer.of().block().get(blockTag);
        if (blocks.isEmpty()) {
            throwException("unknown block tag '" + blockTag.location() + "'");
        }

        var predicateBuilder = ResolvableBlockPredicate.builder(blocks.get());
        return addEntry(offset, predicateBuilder, blockState, query);
    }

    private BlockPatternConditionBuilder addEntry(
        BlockPos offset, ResolvableBlockPredicate.Builder predicateBuilder,
        @Nullable JsonObject blockState, @Nullable String query
    ) {
        if (Math.abs(offset.getX()) > MAX_PATTERN_RADIUS || Math.abs(offset.getY()) > MAX_PATTERN_RADIUS ||
            Math.abs(offset.getZ()) > MAX_PATTERN_RADIUS) {
            throwException("pattern size exceeds maximum allowed radius of " + MAX_PATTERN_RADIUS + " blocks");
        }

        if (blockState != null && !blockState.isEmpty()) {
            var blockStates = predicateBuilder.build().getBlockStates();
            if (!blockStates.isEmpty()) {
                var propertyBuilder = createPropertyBuilder(blockStates.getFirst().getBlock(), blockState);
                predicateBuilder.setProperties(propertyBuilder);
            }
        }

        var patternEntry = new PatternEntry(offset, predicateBuilder.build(), Optional.ofNullable(query));
        if (entries.contains(patternEntry)) {
            throwException("position '" + offset + "' already defined in pattern");
        }

        entries.add(patternEntry);
        return this;
    }

    private StatePropertiesPredicate.Builder createPropertyBuilder(Block block, JsonObject blockState) {
        var definition = block.getStateDefinition();
        var propertyBuilder = StatePropertiesPredicate.Builder.properties();

        for (var entry : blockState.entrySet()) {
            var property = definition.getProperty(entry.getKey());
            if (property == null) {
                var blockId = BuiltInRegistries.BLOCK.wrapAsHolder(block).getRegisteredName();
                throwException("unknown block property '" + entry.getKey() + "' for block '" + blockId + "'");
            }
            propertyBuilder.hasProperty(property, entry.getValue().getAsString());
        }

        return propertyBuilder;
    }

    @HideFromGY
    public BlockPatternCondition build() {
        if (entries.isEmpty()) {
            throwException("block pattern cannot be empty");
        }

        return new BlockPatternCondition(
            List.copyOf(entries),
            Optional.ofNullable(name),
            tooltip == null ? List.of() : tooltip
        );
    }

    private void throwException(String message) throws KubeRuntimeException {
        throw new KubeRuntimeException(message).source(SourceLine.of());
    }
}

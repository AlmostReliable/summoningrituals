package com.almostreliable.summoningrituals.recipe.condition.custom;

import com.almostreliable.summoningrituals.client.tooltip.PatternPreviewTooltipComponent;
import com.almostreliable.summoningrituals.core.Registration;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public final class BlockPatternCheck implements LootItemCondition {

    public static final MapCodec<BlockPatternCheck> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.list(PatternEntry.CODEC).fieldOf("pattern").forGetter(BlockPatternCheck::getPattern),
        ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(BlockPatternCheck::getName)
    ).apply(i, BlockPatternCheck::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPatternCheck> STREAM_CODEC = StreamCodec.composite(
        PatternEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), BlockPatternCheck::getPattern,
        ComponentSerialization.OPTIONAL_STREAM_CODEC, BlockPatternCheck::getName,
        BlockPatternCheck::new
    );

    private final List<PatternEntry> pattern;
    private final Optional<Component> name;
    private @Nullable List<ClientPatternEntry> renderPatternCache;
    private @Nullable PatternPreviewTooltipComponent.Data tooltipComponentCache;

    public BlockPatternCheck(List<PatternEntry> pattern, Optional<Component> name) {
        this.pattern = pattern;
        this.name = name;
    }

    public BlockPatternCheck(List<PatternEntry> pattern) {
        this(pattern, Optional.empty());
    }

    @Override
    public LootItemConditionType getType() {
        return Registration.BLOCK_PATTERN_CONDITION.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    @Override
    public boolean test(LootContext lootContext) {
        var level = lootContext.getLevel();
        var pos = BlockPos.containing(lootContext.getParam(LootContextParams.ORIGIN));

        for (var entry : pattern) {
            var blockOffset = entry.offset;
            var blockPos = pos.offset(blockOffset);
            if (!entry.blockPredicate.matches(level, blockPos)) {
                return false;
            }
        }

        return true;
    }

    public List<PatternEntry> getPattern() {
        return pattern;
    }

    public Optional<Component> getName() {
        return name;
    }

    public List<ClientPatternEntry> getRenderPattern() {
        if (renderPatternCache != null) {
            return renderPatternCache;
        }

        var result = new ArrayList<ClientPatternEntry>();
        for (var entry : pattern) {
            var blockStates = entry.resolveBlockStates();
            result.add(new ClientPatternEntry(blockStates, entry.offset));
        }

        renderPatternCache = Collections.unmodifiableList(result);
        return result;
    }

    public PatternPreviewTooltipComponent.Data getTooltipComponent() {
        if (tooltipComponentCache != null) {
            return tooltipComponentCache;
        }

        var counts = new LinkedHashMap<Block, Integer>();

        for (var entry : getRenderPattern()) {
            var blockStates = entry.blocks();
            if (blockStates.isEmpty()) continue;

            var block = blockStates.getFirst().getBlock();
            counts.merge(block, 1, Integer::sum);
        }

        var stacks = counts.entrySet().stream()
            .map(entry -> new ItemStack(entry.getKey(), entry.getValue()))
            .toList();

        tooltipComponentCache = new PatternPreviewTooltipComponent.Data(stacks);
        return tooltipComponentCache;
    }

    public Collection<PatternEntry> queryEntries(String query) {
        return pattern.stream().filter(e -> e.test(query)).toList();
    }

    public static final class PatternEntry implements Predicate<String> {

        private static final Codec<PatternEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("offset").forGetter(PatternEntry::offset),
            BlockPredicate.CODEC.fieldOf("block_predicate").forGetter(PatternEntry::blockPredicate),
            Codec.STRING.optionalFieldOf("query_id").forGetter(PatternEntry::getQueryId)
        ).apply(i, PatternEntry::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, PatternEntry> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PatternEntry::offset,
            BlockPredicate.STREAM_CODEC, PatternEntry::blockPredicate,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), PatternEntry::getQueryId,
            PatternEntry::new
        );

        private final BlockPos offset;
        private final BlockPredicate blockPredicate;
        private final Optional<String> queryId;
        private @Nullable List<BlockState> cachedBlockStates;

        public PatternEntry(BlockPos offset, BlockPredicate blockPredicate, Optional<String> queryId) {
            this.offset = offset;
            this.blockPredicate = blockPredicate;
            this.queryId = queryId;
        }

        private List<BlockState> resolveBlockStates() {
            if (cachedBlockStates != null) {
                return cachedBlockStates;
            }

            var blockStates = new ArrayList<BlockState>();

            var blockHolderSetOpt = blockPredicate.blocks();
            if (blockHolderSetOpt.isEmpty()) {
                cachedBlockStates = List.of();
                return blockStates;
            }

            var blockHolderSet = blockHolderSetOpt.get();
            var propertyPredicateOpt = blockPredicate.properties();

            blockHolderSet.unwrap()
                .ifLeft(tag -> {
                    for (var blockHolder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
                        resolveBlockState(blockHolder, propertyPredicateOpt, blockStates);
                    }
                })
                .ifRight(blocks -> {
                    for (var blockHolder : blocks) {
                        resolveBlockState(blockHolder, propertyPredicateOpt, blockStates);
                    }
                });

            cachedBlockStates = Collections.unmodifiableList(blockStates);
            return blockStates;
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private static void resolveBlockState(
            Holder<Block> blockHolder, Optional<StatePropertiesPredicate> propertyPredicateOpt, List<BlockState> blockStates
        ) {
            var block = blockHolder.value();
            var blockState = block.defaultBlockState();

            if (propertyPredicateOpt.isEmpty()) {
                blockStates.add(blockState);
                return;
            }

            var propertyPredicate = propertyPredicateOpt.get();
            var propertyMatchers = propertyPredicate.properties();
            if (propertyMatchers.isEmpty()) {
                blockStates.add(blockState);
                return;
            }

            var stateDefinition = block.getStateDefinition();
            for (var propertyMatcher : propertyMatchers) {
                var propertyName = propertyMatcher.name();
                var property = stateDefinition.getProperty(propertyName);
                if (property == null) continue;

                var valueMatcher = propertyMatcher.valueMatcher();
                if (valueMatcher instanceof StatePropertiesPredicate.ExactMatcher(var exactValue)) {
                    blockState = setProperty(blockState, property, exactValue);
                    continue;
                }

                if (valueMatcher instanceof StatePropertiesPredicate.RangedMatcher(var minValueOpt, var maxValueOpt)) {
                    var value = minValueOpt.orElseGet(() -> maxValueOpt.orElse(null));
                    blockState = setProperty(blockState, property, value);
                }
            }

            blockStates.add(blockState);
        }

        private static <T extends Comparable<T>> BlockState setProperty(
            BlockState blockState, Property<T> property, @Nullable String value
        ) {
            if (value == null) return blockState;
            var optional = property.getValue(value);
            return optional.map(v -> blockState.setValue(property, v)).orElse(blockState);
        }

        public BlockPos offset() {
            return offset;
        }

        private BlockPredicate blockPredicate() {
            return blockPredicate;
        }

        private Optional<String> getQueryId() {
            return queryId;
        }

        @Override
        public boolean test(String s) {
            return queryId.isPresent() && queryId.get().equals(s);
        }
    }

    public record ClientPatternEntry(List<BlockState> blocks, BlockPos offset) {}
}

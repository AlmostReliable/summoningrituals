package com.almostreliable.summoningrituals.recipe.condition.custom;

import com.almostreliable.summoningrituals.client.tooltip.PatternPreviewTooltipComponent;
import com.almostreliable.summoningrituals.core.Registration;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class BlockPatternCheck implements LootItemCondition {

    public static final MapCodec<BlockPatternCheck> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.list(PatternEntry.CODEC).fieldOf("pattern").forGetter(BlockPatternCheck::getPattern)
    ).apply(i, BlockPatternCheck::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPatternCheck> STREAM_CODEC = StreamCodec.composite(
        PatternEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), BlockPatternCheck::getPattern,
        BlockPatternCheck::new
    );

    private final List<PatternEntry> pattern;
    private @Nullable List<ClientPatternEntry> renderPatternCache;
    private @Nullable PatternPreviewTooltipComponent.Data tooltipComponentCache;

    public BlockPatternCheck(List<PatternEntry> pattern) {
        this.pattern = pattern;
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

    public List<ClientPatternEntry> getRenderPattern() {
        if (renderPatternCache != null) {
            return renderPatternCache;
        }

        var result = new ArrayList<ClientPatternEntry>();
        for (var entry : pattern) {
            var blockStates = entry.resolveBlockStates();
            result.add(new ClientPatternEntry(blockStates, entry.offset));
        }

        renderPatternCache = result;
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

    public record PatternEntry(BlockPos offset, BlockPredicate blockPredicate) {

        public static final Codec<PatternEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("offset").forGetter(PatternEntry::offset),
            BlockPredicate.CODEC.fieldOf("block_predicate").forGetter(PatternEntry::blockPredicate)
        ).apply(i, PatternEntry::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, PatternEntry> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PatternEntry::offset,
            BlockPredicate.STREAM_CODEC, PatternEntry::blockPredicate,
            PatternEntry::new
        );

        public List<BlockState> resolveBlockStates() {
            var blockStates = new ArrayList<BlockState>();

            var blockHolderSetOpt = blockPredicate.blocks();
            if (blockHolderSetOpt.isEmpty()) return blockStates;
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
    }

    public record ClientPatternEntry(List<BlockState> blocks, BlockPos offset) {}
}

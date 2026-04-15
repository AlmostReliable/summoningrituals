package com.almostreliable.summoningrituals.recipe.condition.pattern;

import com.almostreliable.summoningrituals.core.Constants;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ResolvableBlockPredicate implements Predicate<BlockState> {

    public static final Codec<ResolvableBlockPredicate> CODEC = RecordCodecBuilder.create(i -> i.group(
        RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf(Constants.BLOCKS).forGetter(ResolvableBlockPredicate::getBlocks),
        StatePropertiesPredicate.CODEC.optionalFieldOf(Constants.PROPERTIES).forGetter(ResolvableBlockPredicate::getProperties)
    ).apply(i, ResolvableBlockPredicate::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResolvableBlockPredicate> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.holderSet(Registries.BLOCK), ResolvableBlockPredicate::getBlocks,
        ByteBufCodecs.optional(StatePropertiesPredicate.STREAM_CODEC), ResolvableBlockPredicate::getProperties,
        ResolvableBlockPredicate::new
    );

    private final HolderSet<Block> blocks;
    private final Optional<StatePropertiesPredicate> properties;
    private @Nullable List<BlockState> cachedBlockStates;

    public ResolvableBlockPredicate(HolderSet<Block> blocks, Optional<StatePropertiesPredicate> properties) {
        this.blocks = blocks;
        this.properties = properties;
    }

    public static Builder builder(Block block) {
        return new Builder(block);
    }

    public static Builder builder(TagKey<Block> tag) {
        return new Builder(tag);
    }

    @Override
    public boolean test(BlockState blockState) {
        return blockState.is(blocks) && (properties.isEmpty() || properties.get().matches(blockState));
    }

    private HolderSet<Block> getBlocks() {
        return blocks;
    }

    private Optional<StatePropertiesPredicate> getProperties() {
        return properties;
    }

    public List<BlockState> getBlockStates() {
        if (cachedBlockStates != null) {
            return cachedBlockStates;
        }

        cachedBlockStates = resolveBlockStates();
        return cachedBlockStates;
    }

    private List<BlockState> resolveBlockStates() {
        var blockStates = new ArrayList<BlockState>();
        blocks.unwrap()
            .ifLeft(tag -> {
                for (var blockHolder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
                    resolveBlockState(blockHolder, blockStates);
                }
            })
            .ifRight(blockHolders -> {
                for (var blockHolder : blockHolders) {
                    resolveBlockState(blockHolder, blockStates);
                }
            });

        return Collections.unmodifiableList(blockStates);
    }

    private void resolveBlockState(Holder<Block> blockHolder, List<BlockState> blockStates) {
        var block = blockHolder.value();
        var blockState = block.defaultBlockState();

        if (properties.isEmpty()) {
            blockStates.add(blockState);
            return;
        }

        var propertyPredicate = properties.get();
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

    private static <T extends Comparable<T>> BlockState setProperty(BlockState blockState, Property<T> property, @Nullable String value) {
        if (value == null) return blockState;
        var optional = property.getValue(value);
        return optional.map(v -> blockState.setValue(property, v)).orElse(blockState);
    }

    @SuppressWarnings("deprecation")
    public static final class Builder {

        private final HolderSet<Block> blocks;
        private @Nullable StatePropertiesPredicate.Builder propertiesBuilder;
        private Optional<StatePropertiesPredicate> properties = Optional.empty();

        private Builder(Block block) {
            this.blocks = HolderSet.direct(Block::builtInRegistryHolder, block);
        }

        private Builder(TagKey<Block> tag) {
            this.blocks = BuiltInRegistries.BLOCK.getOrCreateTag(tag);
        }

        public Builder setProperties(StatePropertiesPredicate.Builder properties) {
            this.properties = properties.build();
            return this;
        }

        public ResolvableBlockPredicate build() {
            return new ResolvableBlockPredicate(blocks, properties);
        }
    }
}

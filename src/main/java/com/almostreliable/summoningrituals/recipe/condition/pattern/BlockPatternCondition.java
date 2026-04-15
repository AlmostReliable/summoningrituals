package com.almostreliable.summoningrituals.recipe.condition.pattern;

import com.almostreliable.summoningrituals.altar.AltarBlock;
import com.almostreliable.summoningrituals.client.tooltip.PatternPreviewTooltipComponent;
import com.almostreliable.summoningrituals.compat.kubejs.builder.BlockPatternConditionBuilder;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.network.HighlightPositionsPacket;
import com.almostreliable.summoningrituals.network.PacketHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public final class BlockPatternCondition {

    public static final Codec<BlockPatternCondition> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.list(PatternEntry.CODEC).fieldOf(Constants.ENTRIES).forGetter(BlockPatternCondition::getEntries),
        ComponentSerialization.CODEC.optionalFieldOf(Constants.NAME).forGetter(BlockPatternCondition::getName),
        ComponentSerialization.CODEC.listOf().optionalFieldOf(Constants.TOOLTIP, List.of()).forGetter(BlockPatternCondition::getTooltip)
    ).apply(i, BlockPatternCondition::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPatternCondition> STREAM_CODEC = StreamCodec.composite(
        PatternEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), BlockPatternCondition::getEntries,
        ComponentSerialization.OPTIONAL_STREAM_CODEC, BlockPatternCondition::getName,
        ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()), BlockPatternCondition::getTooltip,
        BlockPatternCondition::new
    );
    private static final Rotation[] ROTATION_VALUES = Rotation.values();

    private final List<PatternEntry> entries;
    private final Optional<Component> name;
    private final List<Component> tooltip;
    private @Nullable PatternPreviewTooltipComponent.Data tooltipComponentCache;

    public BlockPatternCondition(List<PatternEntry> entries, Optional<Component> name, List<Component> tooltip) {
        this.entries = entries;
        this.name = name;
        this.tooltip = tooltip;
    }

    public boolean test(LootContext lootContext, boolean drawHighlights) {
        var level = lootContext.getLevel();
        var altarPos = BlockPos.containing(lootContext.getParam(LootContextParams.ORIGIN));
        var altarState = lootContext.getParam(LootContextParams.BLOCK_STATE);
        var failedPositions = test(level, altarPos, altarState);

        if (!failedPositions.isEmpty() && drawHighlights) {
            PacketHandler.sendToNearbyPlayers(
                level,
                altarPos,
                BlockPatternConditionBuilder.MAX_PATTERN_RADIUS,
                new HighlightPositionsPacket(failedPositions)
            );
            return false;
        }

        return failedPositions.isEmpty();
    }

    public List<BlockPos> test(Level level, BlockPos altarPos, BlockState altarState) {
        var altarFacing = altarState.getValue(AltarBlock.FACING);
        var rotation = getRotation(altarFacing);

        var failedPositions = new ArrayList<BlockPos>();
        for (var entry : entries) {
            var blockPos = altarPos.offset(entry.offset.rotate(rotation));
            var blockState = level.getBlockState(blockPos);

            if (rotation != Rotation.NONE) {
                try {
                    var counterRotation = getCounterRotation(rotation);
                    //noinspection deprecation
                    blockState = blockState.rotate(counterRotation);
                } catch (Exception ignored) {
                    // ignore errors for mods throwing exceptions for unsupported rotation
                }
            }

            if (!entry.predicate.test(blockState)) {
                failedPositions.add(blockPos);
            }
        }

        return failedPositions;
    }

    // exposed for KubeJS
    public List<PatternEntry> getEntries() {
        return entries;
    }

    public Optional<Component> getName() {
        return name;
    }

    public List<Component> getTooltip() {
        return tooltip;
    }

    public Map<BlockPos, List<BlockState>> getPreviewEntries(Direction altarFacing) {
        var rotation = getRotation(altarFacing);
        var result = new HashMap<BlockPos, List<BlockState>>();

        for (var entry : entries) {
            var offset = entry.offset.rotate(rotation);
            var blockStates = entry.predicate.getBlockStates();

            if (rotation != Rotation.NONE) {
                var transformed = new ArrayList<BlockState>();
                for (var blockState : blockStates) {
                    try {
                        //noinspection deprecation
                        transformed.add(blockState.rotate(rotation));
                    } catch (Exception ignored) {
                        // ignore errors for mods throwing exceptions for unsupported rotation
                    }
                }
                blockStates = transformed;
            }

            result.put(offset, blockStates);
        }

        return result;
    }

    public Component getConditionTooltip() {
        var patternTooltip = Component.literal("- ").append(SummoningLang.BLOCK_PATTERN.get().withStyle(ChatFormatting.WHITE));
        name.ifPresent(
            component -> patternTooltip.append(":")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" ").append(component).withStyle(ChatFormatting.AQUA))
        );
        return patternTooltip;
    }

    public Component appendNameTooltip(MutableComponent component) {
        name.ifPresent(n -> component.append(": ").withStyle(ChatFormatting.GOLD).append(n));
        return component;
    }

    public PatternPreviewTooltipComponent.Data getTooltipComponent() {
        if (tooltipComponentCache != null) {
            return tooltipComponentCache;
        }

        var counts = new LinkedHashMap<Block, Integer>();
        for (var entry : entries) {
            var blockStates = entry.predicate.getBlockStates();
            if (blockStates.isEmpty()) continue;
            var block = blockStates.getFirst().getBlock();
            counts.merge(block, 1, Integer::sum);
        }

        var stacks = counts.entrySet()
            .stream()
            .map(entry -> new ItemStack(entry.getKey(), entry.getValue()))
            .toList();

        tooltipComponentCache = new PatternPreviewTooltipComponent.Data(stacks);
        return tooltipComponentCache;
    }

    public Collection<PatternEntry> queryEntries(String query) {
        return entries.stream().filter(e -> e.test(query)).toList();
    }

    private Rotation getRotation(Direction altarFacing) {
        var rotationStep = switch (altarFacing) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> throw new IllegalArgumentException("invalid altar facing: " + altarFacing);
        };
        return ROTATION_VALUES[rotationStep];
    }

    private Rotation getCounterRotation(Rotation rotation) {
        return switch (rotation) {
            case NONE -> Rotation.NONE;
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case CLOCKWISE_180 -> Rotation.CLOCKWISE_180;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
        };
    }

    public record PatternEntry(BlockPos offset, ResolvableBlockPredicate predicate, Optional<String> query) implements Predicate<String> {

        private static final Codec<PatternEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("offset").forGetter(PatternEntry::offset),
            ResolvableBlockPredicate.CODEC.fieldOf("predicate").forGetter(PatternEntry::predicate),
            Codec.STRING.optionalFieldOf("query").forGetter(PatternEntry::query)
        ).apply(i, PatternEntry::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, PatternEntry> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PatternEntry::offset,
            ResolvableBlockPredicate.STREAM_CODEC, PatternEntry::predicate,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), PatternEntry::query,
            PatternEntry::new
        );

        @Override
        public boolean test(String s) {
            return query.isPresent() && query.get().equals(s);
        }

        @Override
        public int hashCode() {
            return offset.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof PatternEntry entry && offset.equals(entry.offset);
        }
    }
}

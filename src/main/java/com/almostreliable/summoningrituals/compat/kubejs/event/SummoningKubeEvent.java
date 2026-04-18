package com.almostreliable.summoningrituals.compat.kubejs.event;

import com.almostreliable.summoningrituals.altar.AltarBlock;
import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.recipe.condition.pattern.BlockPatternCondition;
import com.almostreliable.summoningrituals.recipe.condition.pattern.BlockPatternCondition.PatternEntry;
import com.almostreliable.summoningrituals.recipe.container.RecipeInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.event.KubeEvent;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SummoningKubeEvent implements KubeEvent {

    private final ServerLevel level;
    private final AltarBlockEntity getAltar;
    private final RecipeInfo getRecipeInfo;
    private final @Nullable ServerPlayer getPlayer;

    public SummoningKubeEvent(AltarBlockEntity getAltar, RecipeInfo getRecipeInfo, @Nullable ServerPlayer getPlayer) {
        Preconditions.checkArgument(getAltar.getLevel() instanceof ServerLevel, "altar must be in a server level");
        this.level = (ServerLevel) getAltar.getLevel();
        this.getAltar = getAltar;
        this.getRecipeInfo = getRecipeInfo;
        this.getPlayer = getPlayer;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return getAltar.getBlockPos();
    }

    public AltarBlockEntity getAltar() {
        return getAltar;
    }

    public Direction getAltarFacing() {
        return getAltar().getBlockState().getValue(AltarBlock.FACING);
    }

    public RecipeInfo getRecipeInfo() {
        return getRecipeInfo;
    }

    public @Nullable ServerPlayer getPlayer() {
        return getPlayer;
    }

    public @Nullable List<PatternEntry> getRawBlockPattern() {
        return getRawBlockPattern(getRecipeInfo.getRecipe().blockPattern());
    }

    public @Nullable List<PatternEntry> getRawBlockPatternExtension() {
        return getRawBlockPattern(getRecipeInfo.getRecipe().blockPatternExtension());
    }

    private @Nullable List<PatternEntry> getRawBlockPattern(Optional<BlockPatternCondition> blockPattern) {
        return blockPattern.map(BlockPatternCondition::getRawEntries).orElse(null);
    }

    public @Nullable Map<BlockPos, List<BlockState>> getTransformedBlockPattern() {
        return getTransformedBlockPattern(getRecipeInfo.getRecipe().blockPattern());
    }

    public @Nullable Map<BlockPos, List<BlockState>> getTransformedBlockPatternExtension() {
        return getTransformedBlockPattern(getRecipeInfo.getRecipe().blockPatternExtension());
    }

    private @Nullable Map<BlockPos, List<BlockState>> getTransformedBlockPattern(Optional<BlockPatternCondition> blockPattern) {
        return blockPattern.map(p -> p.getTransformedEntries(getAltarFacing()))
            .orElse(null);
    }

    public @Nullable Collection<BlockPos> queryBlockPattern(String query) {
        return queryBlockPattern(getRecipeInfo.getRecipe().blockPattern(), query);
    }

    public @Nullable Collection<BlockPos> queryBlockPatternExtension(String query) {
        return queryBlockPattern(getRecipeInfo.getRecipe().blockPatternExtension(), query);
    }

    private @Nullable Collection<BlockPos> queryBlockPattern(Optional<BlockPatternCondition> blockPattern, String query) {
        return blockPattern.map(p -> p.queryOffsets(getAltarFacing(), query))
            .orElse(null);
    }
}

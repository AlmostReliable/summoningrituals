package com.almostreliable.summoningrituals.recipe.container;

import com.almostreliable.summoningrituals.data.SummoningLang;

import net.minecraft.world.item.ItemStack;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class RecipeMatchResult {

    public static final RecipeMatchResult INVALID_INITIATOR = new RecipeMatchResult(Set.of(), MatchIssue.INITIATOR);
    public static final RecipeMatchResult MISSING_SACRIFICES = new RecipeMatchResult(Set.of(), MatchIssue.SACRIFICES);
    public static final RecipeMatchResult FAILED_CONDITIONS = new RecipeMatchResult(Set.of(), MatchIssue.CONDITIONS);
    public static final RecipeMatchResult MULTI_MATCH = new RecipeMatchResult(Set.of(), MatchIssue.MULTI);

    private final Set<RecipeInfoContainer> matchingRecipes;
    private final MatchIssue matchIssue;
    @Nullable
    private ItemStack interactionRemainder;

    private RecipeMatchResult(Set<RecipeInfoContainer> matchingRecipes, MatchIssue matchIssue) {
        this.matchingRecipes = matchingRecipes;
        this.matchIssue = matchIssue;
    }

    public static RecipeMatchResult of(Set<RecipeInfoContainer> matchingRecipes) {
        return new RecipeMatchResult(matchingRecipes, MatchIssue.NONE);
    }

    public boolean hasIssue() {
        return matchIssue != MatchIssue.NONE;
    }

    public MatchIssue getMatchIssue() {
        return matchIssue;
    }

    public void setInteractionRemainder(@Nullable ItemStack interactionRemainder) {
        this.interactionRemainder = interactionRemainder;
    }

    @Nullable
    public ItemStack getInteractionRemainder() {
        return interactionRemainder;
    }

    public RecipeInfoContainer getMatchingRecipe() {
        Preconditions.checkState(matchingRecipes.size() == 1, "More than one matching recipe");
        return matchingRecipes.iterator().next();
    }

    public enum MatchIssue {
        INITIATOR(SummoningLang.INVALID_INITIATOR),
        SACRIFICES(SummoningLang.MISSING_SACRIFICES),
        CONDITIONS(SummoningLang.FAILED_CONDITIONS),
        MULTI(SummoningLang.MULTI_MATCH),
        NONE(null);

        @Nullable
        private final SummoningLang.LangEntry langEntry;

        MatchIssue(@Nullable SummoningLang.LangEntry langEntry) {
            this.langEntry = langEntry;
        }

        public SummoningLang.LangEntry getIssueMessage() {
            Preconditions.checkNotNull(langEntry, "Issue has no message");
            return langEntry;
        }
    }
}

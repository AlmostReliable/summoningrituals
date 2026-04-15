package com.almostreliable.summoningrituals.recipe.container;

import com.almostreliable.summoningrituals.data.SummoningLang;

import net.minecraft.world.item.ItemStack;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class RecipeMatch {

    public static final RecipeMatch INVALID_INITIATOR = new RecipeMatch(Set.of(), MatchIssue.INITIATOR);
    public static final RecipeMatch MISSING_SACRIFICES = new RecipeMatch(Set.of(), MatchIssue.SACRIFICES);
    public static final RecipeMatch WRONG_PATTERN = new RecipeMatch(Set.of(), MatchIssue.PATTERN);
    public static final RecipeMatch FAILED_CONDITIONS = new RecipeMatch(Set.of(), MatchIssue.CONDITIONS);
    public static final RecipeMatch MULTI_MATCH = new RecipeMatch(Set.of(), MatchIssue.MULTI);

    private final Set<RecipeInfo> matchingRecipes;
    private final MatchIssue matchIssue;
    private @Nullable ItemStack interactionRemainder;

    private RecipeMatch(Set<RecipeInfo> matchingRecipes, MatchIssue matchIssue) {
        this.matchingRecipes = matchingRecipes;
        this.matchIssue = matchIssue;
    }

    public static RecipeMatch of(Set<RecipeInfo> matchingRecipes) {
        return new RecipeMatch(matchingRecipes, MatchIssue.NONE);
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

    public @Nullable ItemStack getInteractionRemainder() {
        return interactionRemainder;
    }

    public RecipeInfo getRecipeInfo() {
        Preconditions.checkState(matchingRecipes.size() == 1, "more than one matching recipe");
        return matchingRecipes.iterator().next();
    }

    public enum MatchIssue {
        INITIATOR(SummoningLang.INVALID_INITIATOR),
        SACRIFICES(SummoningLang.MISSING_SACRIFICES),
        CONDITIONS(SummoningLang.FAILED_CONDITIONS),
        PATTERN(SummoningLang.WRONG_PATTERN),
        MULTI(SummoningLang.MULTI_MATCH),
        NONE(null);

        private final @Nullable SummoningLang.LangEntry langEntry;

        MatchIssue(@Nullable SummoningLang.LangEntry langEntry) {
            this.langEntry = langEntry;
        }

        public SummoningLang.LangEntry getIssueMessage() {
            Preconditions.checkNotNull(langEntry, "issue has no message");
            return langEntry;
        }
    }
}

package com.almostreliable.summoningrituals.compat.kubegroovy.recipe;

import com.almostreliable.summoningrituals.compat.kubegroovy.builder.BlockPatternConditionBuilder;
import com.almostreliable.summoningrituals.compat.kubegroovy.builder.ConditionsBuilder;
import com.almostreliable.summoningrituals.compat.kubegroovy.wrapper.CommandOutputTypeWrapper;
import com.almostreliable.summoningrituals.core.ModRegistration;
import com.almostreliable.summoningrituals.recipe.input.BaseEntityInput;
import com.almostreliable.summoningrituals.recipe.output.CommandOutput;

import net.minecraft.network.chat.Component;

import dev.uncandango.kubegroovy.error.KubeRuntimeException;
import dev.uncandango.kubegroovy.recipe.KubeRecipe;
import dev.uncandango.kubegroovy.recipe.RecipeScriptContext;
import dev.uncandango.kubegroovy.recipe.filter.RecipeMatchContext;
import dev.uncandango.kubegroovy.recipe.match.ReplacementMatchInfo;
import dev.uncandango.kubegroovy.recipe.schema.KubeRecipeFactory;
import dev.uncandango.kubegroovy.script.SourceLine;
import dev.uncandango.kubegroovy.script.annotations.ReturnsSelf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class AltarKubeRecipe extends KubeRecipe {

    public static final KubeRecipeFactory FACTORY = new KubeRecipeFactory(
        ModRegistration.ALTAR_RECIPE_TYPE.getId(),
        AltarKubeRecipe.class,
        AltarKubeRecipe::new
    );

    @Override
    public void serialize() {
        var entityInputs = getValue(AltarRecipeSchema.ENTITY_INPUTS);
        if (entityInputs != null) {
            for (var i = 0; i < entityInputs.size(); i++) {
                var predicate = entityInputs.get(i).predicate();
                // noinspection ConstantValue
                if (predicate == null) continue;
                BaseEntityInput.PENDING_DATA_VALIDATORS.put(getOrCreateId(), i, predicate);
            }
        }

        var fakeEntityInputs = getValue(AltarRecipeSchema.FAKE_ENTITY_INPUTS);
        if (fakeEntityInputs != null) {
            for (var i = 0; i < fakeEntityInputs.size(); i++) {
                var predicate = fakeEntityInputs.get(i).predicate();
                // noinspection ConstantValue
                if (predicate == null) continue;
                BaseEntityInput.PENDING_FAKE_DATA_VALIDATORS.put(getOrCreateId(), i, predicate);
            }
        }

        super.serialize();
    }

    @Override
    public boolean hasOutput(RecipeMatchContext cx, ReplacementMatchInfo match) {
        for (var value : getRecipeComponentValues()) {
            if (value.key.role.isOutput() && value.matches(cx, match)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean replaceOutput(RecipeScriptContext cx, ReplacementMatchInfo match, Object with) {
        var replaced = false;
        for (var value : getRecipeComponentValues()) {
            if (value.key.role.isOutput()) {
                replaced = value.replace(cx, match, with) || replaced;
            }
        }

        if (replaced) {
            save();
        }
        return replaced;
    }

    @ReturnsSelf
    public AltarKubeRecipe commands(CommandOutput commands) {
        setValue(AltarRecipeSchema.COMMANDS, commands);
        return this;
    }

    @ReturnsSelf
    public AltarKubeRecipe commands(List<String> commands, List<Component> tooltip) {
        var normalized = normalizeCommandList(commands);
        setValue(AltarRecipeSchema.COMMANDS, new CommandOutput(normalized, tooltip));
        return this;
    }

    @ReturnsSelf
    public AltarKubeRecipe commands(List<String> commands, List<Component> tooltip, boolean requiresPlayer) {
        var normalized = normalizeCommandList(commands);
        setValue(AltarRecipeSchema.COMMANDS, new CommandOutput(normalized, tooltip, requiresPlayer));
        return this;
    }

    @ReturnsSelf
    public AltarKubeRecipe conditions(UnaryOperator<ConditionsBuilder> conditions) {
        setValue(AltarRecipeSchema.CONDITIONS, conditions.apply(new ConditionsBuilder()).build());
        return this;
    }

    @ReturnsSelf
    public AltarKubeRecipe blockPattern(UnaryOperator<BlockPatternConditionBuilder> blockPattern) {
        var pattern = blockPattern.apply(new BlockPatternConditionBuilder()).build();
        setValue(AltarRecipeSchema.BLOCK_PATTERN, pattern);
        return this;
    }

    @ReturnsSelf
    public AltarKubeRecipe blockPatternExtension(UnaryOperator<BlockPatternConditionBuilder> blockPattern) {
        if (getValue(AltarRecipeSchema.BLOCK_PATTERN) == null) {
            throw new KubeRuntimeException("cannot set block pattern extension without a main block pattern").source(SourceLine.of());
        }

        var pattern = blockPattern.apply(new BlockPatternConditionBuilder()).build();
        setValue(AltarRecipeSchema.BLOCK_PATTERN_EXTENSION, pattern);
        return this;
    }

    private static List<String> normalizeCommandList(List<String> commands) {
        var result = new ArrayList<String>();
        for (var command : commands) {
            result.add(CommandOutputTypeWrapper.normalizeCommand(command));
        }
        return result;
    }
}

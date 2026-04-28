package com.almostreliable.summoningrituals.compat.kubejs.recipe;

import com.almostreliable.summoningrituals.compat.kubejs.builder.BlockPatternConditionBuilder;
import com.almostreliable.summoningrituals.compat.kubejs.builder.ConditionsBuilder;
import com.almostreliable.summoningrituals.compat.kubejs.wrapper.CommandOutputTypeWrapper;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.recipe.input.BaseEntityInput;
import com.almostreliable.summoningrituals.recipe.output.CommandOutput;

import net.minecraft.network.chat.Component;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.util.ReturnsSelf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class AltarKubeRecipe extends KubeRecipe {

    public static final KubeRecipeFactory FACTORY = new KubeRecipeFactory(
        Registration.ALTAR_RECIPE_TYPE.getId(),
        AltarKubeRecipe.class,
        AltarKubeRecipe::new
    );

    @Override
    public void serialize() {
        var entityInputs = getValue(AltarRecipeSchema.ENTITY_INPUTS);
        if (entityInputs != null) {
            for (var i = 0; i < entityInputs.size(); i++) {
                var predicate = entityInputs.get(i).predicate();
                //noinspection ConstantValue
                if (predicate == null) continue;
                BaseEntityInput.DATA_VALIDATORS.put(getOrCreateId(), i, predicate);
            }
        }

        var fakeEntityInputs = getValue(AltarRecipeSchema.FAKE_ENTITY_INPUTS);
        if (fakeEntityInputs != null) {
            for (var i = 0; i < fakeEntityInputs.size(); i++) {
                var predicate = fakeEntityInputs.get(i).predicate();
                //noinspection ConstantValue
                if (predicate == null) continue;
                BaseEntityInput.FAKE_DATA_VALIDATORS.put(getOrCreateId(), i, predicate);
            }
        }

        super.serialize();
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
    public AltarKubeRecipe conditions(Context ctx, UnaryOperator<ConditionsBuilder> conditions) {
        setValue(AltarRecipeSchema.CONDITIONS, conditions.apply(new ConditionsBuilder()).build(ctx));
        return this;
    }

    @ReturnsSelf
    public AltarKubeRecipe blockPattern(Context ctx, UnaryOperator<BlockPatternConditionBuilder> blockPattern) {
        var pattern = blockPattern.apply(new BlockPatternConditionBuilder()).build(ctx);
        setValue(AltarRecipeSchema.BLOCK_PATTERN, pattern);
        return this;
    }

    @ReturnsSelf
    public AltarKubeRecipe blockPatternExtension(Context ctx, UnaryOperator<BlockPatternConditionBuilder> blockPattern) {
        if (getValue(AltarRecipeSchema.BLOCK_PATTERN) == null) {
            throw new KubeRuntimeException("cannot set block pattern extension without a main block pattern").source(SourceLine.of(ctx));
        }

        var pattern = blockPattern.apply(new BlockPatternConditionBuilder()).build(ctx);
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

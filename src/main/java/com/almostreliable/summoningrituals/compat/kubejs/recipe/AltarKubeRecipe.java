package com.almostreliable.summoningrituals.compat.kubejs.recipe;

import com.almostreliable.summoningrituals.compat.kubejs.builder.ConditionsBuilder;
import com.almostreliable.summoningrituals.compat.kubejs.wrapper.CommandOutputTypeWrapper;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.recipe.output.CommandOutput;

import net.minecraft.network.chat.Component;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.rhino.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AltarKubeRecipe extends KubeRecipe {

    public static final KubeRecipeFactory FACTORY = new KubeRecipeFactory(
        Registration.ALTAR_RECIPE_TYPE.getId(),
        AltarKubeRecipe.class,
        AltarKubeRecipe::new
    );

    public AltarKubeRecipe commands(CommandOutput commands) {
        setValue(AltarRecipeSchema.COMMANDS, commands);
        return this;
    }

    public AltarKubeRecipe commands(List<String> commands, List<Component> tooltip) {
        var normalized = normalizeCommandList(commands);
        setValue(AltarRecipeSchema.COMMANDS, new CommandOutput(normalized, tooltip));
        return this;
    }

    public AltarKubeRecipe commands(List<String> commands, List<Component> tooltip, boolean requiresPlayer) {
        var normalized = normalizeCommandList(commands);
        setValue(AltarRecipeSchema.COMMANDS, new CommandOutput(normalized, tooltip, requiresPlayer));
        return this;
    }

    public AltarKubeRecipe command(CommandOutput commands) {
        return commands(commands);
    }

    public AltarKubeRecipe command(List<String> commands, List<Component> tooltip) {
        return commands(commands, tooltip);
    }

    public AltarKubeRecipe command(List<String> commands, List<Component> tooltip, boolean requiresPlayer) {
        return commands(commands, tooltip, requiresPlayer);
    }

    public AltarKubeRecipe conditions(Context ctx, Function<ConditionsBuilder, ConditionsBuilder> conditions) {
        setValue(AltarRecipeSchema.CONDITIONS, conditions.apply(new ConditionsBuilder()).build(ctx));
        return this;
    }

    public static List<String> normalizeCommandList(List<String> commands) {
        var result = new ArrayList<String>();
        for (var command : commands) {
            result.add(CommandOutputTypeWrapper.normalizeCommand(command));
        }
        return result;
    }
}

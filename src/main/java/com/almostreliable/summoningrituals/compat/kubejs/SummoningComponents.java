package com.almostreliable.summoningrituals.compat.kubejs;

import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs;
import com.almostreliable.summoningrituals.recipe.component.RecipeSacrifices;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.kubejs.util.MapJS;

import java.util.Map;

/**
 * Components for Summoning Rituals KubeJS recipes.<br>
 * When using these, note that this implementation is intended to be excluded from autogenerated builder methods!
 */
public interface SummoningComponents {

    RecipeComponent<RecipeSacrifices> SACRIFICES = new RecipeComponent<>() {

        @Override
        public Class<?> componentClass() {
            return RecipeSacrifices.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, RecipeSacrifices value) {
            return value.toJson();
        }

        @Override
        public RecipeSacrifices read(RecipeJS recipe, Object from) {
            if (from instanceof RecipeSacrifices sac) {
                return sac;
            }

            if (from instanceof Map<?, ?> || from instanceof JsonObject) {
                var json = MapJS.json(from);
                if (json != null) {
                    return RecipeSacrifices.fromJson(json);
                }
            }

            // return an empty "new" sacrifices object
            return new RecipeSacrifices();
        }
    };

    RecipeComponent<BlockReference> BLOCK_REFERENCE = new RecipeComponent<>() {

        @Override
        public Class<?> componentClass() {
            return BlockReference.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, BlockReference value) {
            return value.toJson();
        }

        @Override
        public BlockReference read(RecipeJS recipe, Object from) {
            if (from instanceof BlockReference blockReference) {
                return blockReference;
            }

            if (from instanceof Map<?, ?> || from instanceof JsonObject) {
                var json = MapJS.json(from);
                if (json != null) {
                    return BlockReference.fromJson(json);
                }
            }

            throw new RecipeExceptionJS("Invalid block reference: " + from);
        }
    };

    RecipeComponent<RecipeOutputs> OUTPUTS = new RecipeComponent<>() {

        @Override
        public Class<?> componentClass() {
            return RecipeOutputs.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, RecipeOutputs value) {
            return value.toJson();
        }

        @Override
        public RecipeOutputs read(RecipeJS recipe, Object from) {
            if (from instanceof RecipeOutputs outputs) {
                return outputs;
            }

            var asList = ListJS.json(from);

            if (asList != null) {
                return RecipeOutputs.fromJson(asList);
            }

            throw new RecipeExceptionJS("Invalid outputs: " + from);
        }
    };
}
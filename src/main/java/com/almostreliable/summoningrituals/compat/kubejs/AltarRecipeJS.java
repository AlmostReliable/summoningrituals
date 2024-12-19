package com.almostreliable.summoningrituals.compat.kubejs;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.platform.Platform;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs.ItemOutputBuilder;
import com.almostreliable.summoningrituals.recipe.component.RecipeOutputs.MobOutputBuilder;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;

@SuppressWarnings({"unused", "WeakerAccess"})
public class AltarRecipeJS extends RecipeJS {

    @Override
    public InputItem readInputItem(Object from) {
        if (from instanceof JsonElement jsonElement) {
            var stack = IngredientStack.fromJson(jsonElement);
            return InputItem.of(stack.ingredient(), stack.count());
        }
        return super.readInputItem(from);
    }

    @Override
    public JsonElement writeInputItem(InputItem value) {
        if (value.count == 1) {
            return value.ingredient.toJson();
        } else {
            var obj = new JsonObject();
            obj.add("ingredient", value.ingredient.toJson());
            obj.addProperty("count", value.count);
            return obj;
        }
    }

    public AltarRecipeJS itemOutput(ItemOutputBuilder itemOutput) {
        getValue(AltarRecipeSchema.OUTPUTS).add(itemOutput.build());
        return this;
    }

    public AltarRecipeJS mobOutput(MobOutputBuilder entityOutput) {
        getValue(AltarRecipeSchema.OUTPUTS).add(entityOutput.build());
        return this;
    }

    public AltarRecipeJS input(InputItem... ingredients) {
        var arr = ArrayUtils.addAll(getValue(AltarRecipeSchema.INPUTS), ingredients);
        setValue(AltarRecipeSchema.INPUTS, arr);
        return this;
    }

    public AltarRecipeJS sacrifice(ResourceLocation id, int count) {
        Preconditions.checkNotNull(id);
        getValue(AltarRecipeSchema.SACRIFICES).add(Platform.mobFromId(id), count);
        return this;
    }

    public AltarRecipeJS sacrifice(ResourceLocation id) {
        Preconditions.checkNotNull(id);
        return sacrifice(id, 1);
    }

    public AltarRecipeJS sacrificeRegion(int width, int height) {
        getValue(AltarRecipeSchema.SACRIFICES).setRegion(new Vec3i(width, height, width));
        return this;
    }

    public AltarRecipeJS blockBelow(ResourceLocation id, JsonObject properties) {
        Preconditions.checkNotNull(id);
        var blockJson = new JsonObject();
        blockJson.addProperty(Constants.BLOCK, id.toString());
        blockJson.add(Constants.PROPERTIES, properties);
        setValue(AltarRecipeSchema.BLOCK_BELOW, BlockReference.fromJson(blockJson));
        return this;
    }

    public AltarRecipeJS blockBelow(ResourceLocation id) {
        return blockBelow(id, new JsonObject());
    }
}

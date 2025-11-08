package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningItemBinding;
import com.almostreliable.summoningrituals.compat.kubejs.builder.ItemOutputBuilder;
import com.almostreliable.summoningrituals.recipe.output.ItemOutput;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;

public class ItemOutputComponent implements RecipeComponent<ItemOutput> {

    public static final ItemOutputComponent INSTANCE = new ItemOutputComponent();

    @Override
    public Codec<ItemOutput> codec() {
        return ItemOutput.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ItemOutput.class).or(TypeInfo.of(ItemOutputBuilder.class)).or(ItemStackJS.TYPE_INFO);
    }

    @Override
    public ItemOutput wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from instanceof ItemOutput o) {
            return o;
        }

        if (from instanceof ItemOutputBuilder builder) {
            return builder.build();
        }

        var registryAccess = ((KubeJSContext) cx).getRegistries();
        var stack = ItemStackJS.wrap(registryAccess, from);
        if (stack.isEmpty()) {
            throw new KubeRuntimeException("empty summoning item output: " + from).source(recipe.sourceLine);
        }

        return SummoningItemBinding.of(stack).build();
    }

    @Override
    public String toString() {
        return SummoningRituals.getRL("item_output").toString();
    }
}

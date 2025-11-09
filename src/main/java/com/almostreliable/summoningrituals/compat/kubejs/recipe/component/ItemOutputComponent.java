package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningItemBinding;
import com.almostreliable.summoningrituals.compat.kubejs.builder.ItemOutputBuilder;
import com.almostreliable.summoningrituals.recipe.output.ItemOutput;

import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.ItemWrapper;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public record ItemOutputComponent(RecipeComponentType<?> type) implements RecipeComponent<ItemOutput> {

    public static final RecipeComponentType<ItemOutput> TYPE = RecipeComponentType.unit(
        SummoningRituals.getRL("item_output"),
        ItemOutputComponent::new
    );
    private static final ItemOutput EMPTY = SummoningItemBinding.of(ItemStack.EMPTY).build();

    @Override
    public Codec<ItemOutput> codec() {
        return ItemOutput.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ItemOutput.class).or(TypeInfo.of(ItemOutputBuilder.class)).or(ItemWrapper.TYPE_INFO);
    }

    @Override
    public ItemOutput wrap(RecipeScriptContext cx, Object from) {
        if (from instanceof ItemOutput o) {
            return o;
        }

        if (from instanceof ItemOutputBuilder builder) {
            return builder.build();
        }

        var stack = ItemWrapper.wrap(cx.cx(), from);
        return stack.isEmpty() ? EMPTY : SummoningItemBinding.of(stack).build();
    }
}

package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningItemBinding;
import com.almostreliable.summoningrituals.compat.kubejs.builder.ItemOutputBuilder;
import com.almostreliable.summoningrituals.recipe.output.ItemOutput;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.ItemWrapper;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public record ItemOutputComponent(ResourceKey<RecipeComponentType<?>> type) implements RecipeComponent<ItemOutput> {

    public static final ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(SummoningRituals.getRL("item_output"));
    public static final ItemOutputComponent INSTANCE = new ItemOutputComponent(TYPE);
    public static final ItemStackTemplate EMPTY = new ItemStackTemplate(Items.BARRIER);
    private static final Codec<ItemOutput> CODEC = ItemOutput.CODEC.validate(output -> output.item() == EMPTY
        ? DataResult.error(() -> "Item output must not be empty")
        : DataResult.success(output));

    public static ItemStackTemplate templateOf(ItemStack stack) {
        return stack.isEmpty() ? EMPTY : ItemStackTemplate.fromNonEmptyStack(stack);
    }

    @Override
    public Codec<ItemOutput> codec() {
        return CODEC;
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
        return SummoningItemBinding.of(stack).build();
    }
}

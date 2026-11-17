package com.almostreliable.summoningrituals.compat.kubejs.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubejs.binding.SummoningItemBinding;
import com.almostreliable.summoningrituals.compat.kubejs.builder.ItemOutputBuilder;
import com.almostreliable.summoningrituals.recipe.output.ItemOutput;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.ItemWrapper;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.recipe.match.ItemMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.rhino.type.TypeInfo;

import org.jspecify.annotations.Nullable;

public record ItemOutputComponent(ResourceKey<RecipeComponentType<?>> type) implements RecipeComponent<ItemOutput> {

    public static final ResourceKey<RecipeComponentType<?>> TYPE = RecipeComponentType.key(SummoningRituals.getRL("item_output"));
    public static final ItemOutputComponent INSTANCE = new ItemOutputComponent(TYPE);

    public static ItemStackTemplate templateOf(ItemStack stack) {
        if (stack.isEmpty()) throw new KubeRuntimeException("item must not be empty");
        return ItemStackTemplate.fromNonEmptyStack(stack);
    }

    @Override
    public Codec<ItemOutput> codec() {
        return ItemOutput.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ItemOutput.class).or(TypeInfo.of(ItemOutputBuilder.class)).or(ItemWrapper.TYPE_INFO);
    }

    @Override
    public ItemOutput wrap(RecipeScriptContext cx, @Nullable Object from) {
        if (from instanceof ItemOutput o) {
            return o;
        }

        if (from instanceof ItemOutputBuilder builder) {
            return builder.build();
        }

        var stack = ItemWrapper.wrap(cx.cx(), from);
        return SummoningItemBinding.of(stack).build();
    }

    @Override
    public boolean matches(RecipeMatchContext cx, ItemOutput value, ReplacementMatchInfo match) {
        return match.match() instanceof ItemMatch m && m.matches(cx, value.item().create(), match.exact());
    }

    @Override
    public ItemOutput replace(RecipeScriptContext cx, ItemOutput original, ReplacementMatchInfo match, Object with) {
        if (!matches(cx, original, match)) {
            return original;
        }

        var stack = ItemWrapper.wrap(cx.cx(), with).copyWithCount(original.item().count());
        return new ItemOutput(templateOf(stack), original.offset(), original.spread());
    }
}

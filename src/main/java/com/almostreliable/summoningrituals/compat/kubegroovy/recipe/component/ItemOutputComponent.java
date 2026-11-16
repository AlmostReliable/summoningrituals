package com.almostreliable.summoningrituals.compat.kubegroovy.recipe.component;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.compat.kubegroovy.binding.SummoningItemBinding;
import com.almostreliable.summoningrituals.compat.kubegroovy.builder.ItemOutputBuilder;
import com.almostreliable.summoningrituals.recipe.output.ItemOutput;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import com.mojang.serialization.Codec;
import dev.uncandango.kubegroovy.error.KubeRuntimeException;
import dev.uncandango.kubegroovy.plugin.builtin.wrapper.ItemWrapper;
import dev.uncandango.kubegroovy.recipe.RecipeScriptContext;
import dev.uncandango.kubegroovy.recipe.component.RecipeComponent;
import dev.uncandango.kubegroovy.recipe.component.RecipeComponentType;
import dev.uncandango.kubegroovy.script.type.TypeInfo;

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

        var stack = ItemWrapper.wrap(from);
        return SummoningItemBinding.of(stack).build();
    }
}

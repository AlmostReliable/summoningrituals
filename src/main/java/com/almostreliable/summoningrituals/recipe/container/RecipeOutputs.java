package com.almostreliable.summoningrituals.recipe.container;

import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.output.CommandOutput;
import com.almostreliable.summoningrituals.recipe.output.EntityOutput;
import com.almostreliable.summoningrituals.recipe.output.ItemOutput;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record RecipeOutputs(
    List<ItemOutput> itemOutputs, List<EntityOutput> entityOutputs, Optional<CommandOutput> commandOutput, List<ItemStack> displayOutputs
) {

    public static final MapCodec<RecipeOutputs> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        ItemOutput.CODEC.listOf().optionalFieldOf(Constants.ITEM_OUTPUTS, List.of()).forGetter(RecipeOutputs::itemOutputs),
        EntityOutput.CODEC.listOf().optionalFieldOf(Constants.ENTITY_OUTPUTS, List.of()).forGetter(RecipeOutputs::entityOutputs),
        CommandOutput.CODEC.optionalFieldOf(Constants.COMMANDS).forGetter(RecipeOutputs::commandOutput),
        ItemStack.STRICT_CODEC.listOf().optionalFieldOf(Constants.DISPLAY_OUTPUTS, List.of()).forGetter(RecipeOutputs::displayOutputs)
    ).apply(i, RecipeOutputs::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeOutputs> STREAM_CODEC = StreamCodec.composite(
        ItemOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeOutputs::itemOutputs,
        EntityOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeOutputs::entityOutputs,
        ByteBufCodecs.optional(CommandOutput.STREAM_CODEC), RecipeOutputs::commandOutput,
        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeOutputs::displayOutputs,
        RecipeOutputs::new
    );
}

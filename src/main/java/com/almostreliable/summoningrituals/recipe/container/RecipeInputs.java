package com.almostreliable.summoningrituals.recipe.container;

import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.input.BaseEntityInput;
import com.almostreliable.summoningrituals.recipe.input.EntityInput;
import com.almostreliable.summoningrituals.recipe.input.FakeEntityInput;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record RecipeInputs(List<SizedIngredient> itemInputs, List<EntityInput> entityInputs, List<FakeEntityInput> fakeEntityInputs) {

    public static final MapCodec<RecipeInputs> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        SizedIngredient.FLAT_CODEC.listOf().optionalFieldOf(Constants.ITEM_INPUTS, List.of()).forGetter(RecipeInputs::itemInputs),
        EntityInput.CODEC.listOf().optionalFieldOf(Constants.ENTITY_INPUTS, List.of()).forGetter(RecipeInputs::entityInputs),
        FakeEntityInput.CODEC.listOf().optionalFieldOf(Constants.FAKE_ENTITY_INPUTS, List.of()).forGetter(RecipeInputs::fakeEntityInputs)
    ).apply(i, RecipeInputs::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeInputs> STREAM_CODEC = StreamCodec.composite(
        SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeInputs::itemInputs,
        EntityInput.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeInputs::entityInputs,
        FakeEntityInput.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeInputs::fakeEntityInputs,
        RecipeInputs::new
    );

    @Nullable
    public List<Entity> getSacrifices(AABB zoneRegion, ResourceLocation recipeId, Function<AABB, List<Entity>> entityCollector) {
        if (entityInputs.isEmpty() && fakeEntityInputs.isEmpty()) return List.of();

        var remainingEntities = new ArrayList<>(entityCollector.apply(zoneRegion));

        var entityInputSacrifices = consumeSacrifices(recipeId, entityInputs, e -> Optional.of(e.entityInfo().count()), remainingEntities);
        if (entityInputSacrifices == null) return null;
        var sacrifices = new ArrayList<>(entityInputSacrifices);

        var fakeEntityInputSacrifices = consumeSacrifices(recipeId, fakeEntityInputs, FakeEntityInput::count, remainingEntities);
        if (fakeEntityInputSacrifices == null) return null;
        sacrifices.addAll(fakeEntityInputSacrifices);

        return sacrifices;
    }

    @Nullable
    private static <T extends BaseEntityInput> List<Entity> consumeSacrifices(
        ResourceLocation recipeId, List<T> inputs, Function<T, Optional<Integer>> countSupplier, List<Entity> entities
    ) {
        var sacrifices = new ArrayList<Entity>();

        for (var i = 0; i < inputs.size(); i++) {
            var input = inputs.get(i);
            var requiredCount = countSupplier.apply(input);
            var inputIndex = i;

            var matchStream = entities.stream()
                .filter(e -> input.test(recipeId, inputIndex, e));
            var matches = (requiredCount.isPresent() ? matchStream.limit(requiredCount.get()) : matchStream)
                .toList();

            if (requiredCount.isPresent() && matches.size() < requiredCount.get()) return null;

            sacrifices.addAll(matches);
            entities.removeAll(matches);
        }

        return sacrifices;
    }
}

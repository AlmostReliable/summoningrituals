package com.almostreliable.summoningrituals.recipe;

import com.almostreliable.summoningrituals.Registration;
import com.almostreliable.summoningrituals.inventory.AltarInventory;
import com.almostreliable.summoningrituals.recipe.component.EntityOutput;
import com.almostreliable.summoningrituals.recipe.component.ItemOutput;
import com.almostreliable.summoningrituals.recipe.component.RecipeSacrifices;
import com.almostreliable.summoningrituals.util.GameUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record AltarRecipe(Ingredient catalyst, List<ItemOutput> itemOutputs, List<EntityOutput> entityOutputs, List<SizedIngredient> inputs,
                          Optional<RecipeSacrifices> sacrifices, int recipeTime, Optional<BlockPredicate> blockBelow, DAY_TIME dayTime,
                          WEATHER weather) implements Recipe<AltarInventory> {

    public static final Set<Ingredient> CATALYST_CACHE = new HashSet<>();

    @Override
    public boolean matches(AltarInventory inv, Level level) {
        return false;
        // if (inv.getCatalyst().isEmpty() || !catalyst.test(inv.getCatalyst())) {
        //     return false;
        // }
        //
        // var matchedItems = new Ingredient[inv.getContainerSize()];
        // List<Ingredient> matchedIngredients = new ArrayList<>();
        //
        // for (var slot = 0; slot < inv.getItems().size(); slot++) {
        //     var stack = inv.getItems().get(slot);
        //     if (!stack.isEmpty() && matchedItems[slot] == null) {
        //         for (var input : inputs) {
        //             if (!matchedIngredients.contains(input.ingredient()) &&
        //                 input.ingredient().test(stack) && stack.getCount() >= input.count()) {
        //                 matchedItems[slot] = input.ingredient();
        //                 matchedIngredients.add(input.ingredient());
        //             }
        //         }
        //     }
        // }
        //
        // return matchedIngredients.size() == inputs.size();
    }

    @Override
    public ItemStack assemble(AltarInventory input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.ALTAR_RECIPE.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return Registration.ALTAR_RECIPE.type().get();
    }

    public enum WEATHER implements StringRepresentable {
        ANY,
        CLEAR,
        RAIN,
        THUNDER;

        public static final Codec<WEATHER> CODEC = StringRepresentable.fromEnum(WEATHER::values);
        public static final StreamCodec<ByteBuf, WEATHER> STREAM_CODEC = ByteBufCodecs.idMapper(
            value -> WEATHER.values()[value],
            WEATHER::ordinal
        );

        public boolean check(Level level, @Nullable ServerPlayer player) {
            var check = switch (this) {
                case ANY -> true;
                case CLEAR -> !level.isRaining() && !level.isThundering();
                case RAIN -> level.isRaining();
                case THUNDER -> level.isThundering();
            };
            if (!check) {
                GameUtils.sendPlayerMessage(
                    player,
                    toString().toLowerCase(),
                    ChatFormatting.YELLOW
                );
            }
            return check;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    public enum DAY_TIME implements StringRepresentable {
        ANY,
        DAY,
        NIGHT;

        public static final Codec<DAY_TIME> CODEC = StringRepresentable.fromEnum(DAY_TIME::values);
        public static final StreamCodec<ByteBuf, DAY_TIME> STREAM_CODEC = ByteBufCodecs.idMapper(
            value -> DAY_TIME.values()[value],
            DAY_TIME::ordinal
        );

        public boolean check(Level level, @Nullable ServerPlayer player) {
            var check = switch (this) {
                case ANY -> true;
                case DAY -> level.isDay();
                case NIGHT -> level.isNight();
            };
            if (!check) {
                GameUtils.sendPlayerMessage(
                    player,
                    toString().toLowerCase(),
                    ChatFormatting.YELLOW
                );
            }
            return check;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}

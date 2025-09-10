package com.almostreliable.summoningrituals.altar;

import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.inventory.AltarInventory;
import com.almostreliable.summoningrituals.platform.Platform;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.util.GameUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class AltarBlockEntity extends BlockEntity implements TickableBlockEntity {

    public static final AltarObservable SUMMONING_START = new AltarObservable();
    public static final AltarObservable SUMMONING_COMPLETE = new AltarObservable();

    // TODO: implement dropping contents on destroy
    private final AltarInventory inventory;

    @Nullable
    private AltarRecipe currentRecipe;
    @Nullable
    private List<EntitySacrifice> sacrifices;
    @Nullable
    private ServerPlayer invokingPlayer;
    private int recipeProgress;
    private int recipeTime;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.ALTAR_BLOCK_ENTITY.get(), pos, state);
        this.inventory = new AltarInventory(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(Constants.INVENTORY, inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(Constants.INVENTORY)) inventory.deserializeNBT(registries, tag.getCompound(Constants.INVENTORY));
    }

    @Override
    public void tick(ServerLevel level) {
        if (!inventory.getCatalyst().isEmpty() && currentRecipe == null) {
            var recipe = findRecipe();
            if (recipe == null) {
                resetSummoning(true);
                return;
            }
            handleSummoning(recipe, null);
        }
        if (currentRecipe == null) return;

        if (recipeProgress >= currentRecipe.recipeTime()) {
            if (inventory.handleRecipe(currentRecipe)) {
                // currentRecipe.outputs().handleRecipe((ServerLevel) level, worldPosition);
                SUMMONING_COMPLETE.invoke((ServerLevel) level, worldPosition, currentRecipe, invokingPlayer);
                GameUtils.playSound(level, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP);
                resetSummoning(false);
            } else {
                GameUtils.sendPlayerMessage(invokingPlayer, Constants.INVALID, ChatFormatting.RED);
                resetSummoning(true);
            }
            return;
        }

        if (recipeProgress == 0) {
            changeActivityState(true);
            if (sacrifices != null && !sacrifices.isEmpty()) {
                sacrifices.stream()
                    .map(EntitySacrifice::kill)
                    .filter(positions -> !positions.isEmpty())
                    .forEach(p -> Platform.sendParticleEmit(level, p));
            }
        }
        recipeProgress++;
        Platform.sendProgressUpdate(level, worldPosition, recipeProgress);
    }

    public ItemStack handleInteraction(@Nullable ServerPlayer player, ItemStack stack) {
        if (recipeProgress > 0) {
            GameUtils.sendPlayerMessage(player, Constants.PROGRESS, ChatFormatting.RED);
            return stack;
        }

        if (stack.isEmpty()) {
            if (player != null && player.isShiftKeyDown()) {
                inventory.popLastInserted();
            }
            return ItemStack.EMPTY;
        }

        if (AltarRecipe.CATALYSTS.stream().anyMatch(ingredient -> ingredient.test(stack))) {
            inventory.setCatalyst(stack.copyWithCount(1));
            var recipe = findRecipe();
            if (recipe == null) {
                inventory.setCatalyst(ItemStack.EMPTY);
            } else {
                handleSummoning(recipe, player);
                var remainder = stack.copyWithCount(stack.getCount() - 1);
                return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
            }
        }

        var remaining = inventory.handleInsertion(stack);
        if (player != null && (remaining.isEmpty() || stack.getCount() != remaining.getCount())) {
            GameUtils.playSound(level, worldPosition, SoundEvents.ITEM_PICKUP);
        }
        return remaining;
    }

    private void resetSummoning(boolean popLastInserted) {
        assert level != null;
        currentRecipe = null;
        sacrifices = null;
        invokingPlayer = null;
        recipeProgress = 0;
        Platform.sendProgressUpdate(level, worldPosition, recipeProgress);
        recipeTime = 0;
        Platform.sendProcessTimeUpdate(level, worldPosition, recipeTime);
        changeActivityState(false);
        if (popLastInserted) inventory.popLastInserted();
    }

    private void handleSummoning(AltarRecipe recipe, @Nullable ServerPlayer player) {
        assert level != null && !level.isClientSide;

        // sacrifices = checkSacrifices(recipe.sacrifices(), player);
        // if (sacrifices == null ||
        //     !checkBlockBelow(recipe.blockBelow(), player) ||
        //     !recipe.dayTime().check(level, player) ||
        //     !recipe.weather().check(level, player)) {
        //     inventory.popLastInserted();
        //     GameUtils.playSound(level, worldPosition, SoundEvents.CHAIN_BREAK);
        //     return;
        // }

        if (!SUMMONING_START.invoke((ServerLevel) level, worldPosition, recipe, player)) {
            resetSummoning(true);
            return;
        }
        currentRecipe = recipe;
        invokingPlayer = player;
        recipeTime = recipe.recipeTime();
        GameUtils.playSound(level, worldPosition, SoundEvents.BEACON_ACTIVATE);
        Platform.sendProcessTimeUpdate(level, worldPosition, recipeTime);
    }

    @Nullable
    private AltarRecipe findRecipe() {
        return null;
        // assert level != null && !level.isClientSide;
        // var recipeManager = level.getRecipeManager();
        // return recipeManager.getRecipeFor(Registration.ALTAR_RECIPE_TYPE.get(), inventory, level)
        //     .orElse(null);
    }

    // @Nullable
    // private List<EntitySacrifice> checkSacrifices(RecipeSacrifices sacrifices, @Nullable ServerPlayer player) {
    //     assert level != null && !level.isClientSide;
    //     if (sacrifices.isEmpty()) return List.of();
    //     var region = sacrifices.getRegion(worldPosition);
    //     var entities = level.getEntities(player, region);
    //     List<EntitySacrifice> toKill = new ArrayList<>();
    //     var success = sacrifices.test(sacrifice -> {
    //         var found = entities.stream().filter(sacrifice).toList();
    //         if (found.size() < sacrifice.count()) {
    //             GameUtils.sendPlayerMessage(player, Constants.SACRIFICES, ChatFormatting.YELLOW);
    //             return false;
    //         }
    //         toKill.add(new EntitySacrifice(found, sacrifice.count()));
    //         return true;
    //     });
    //     return success ? toKill : null;
    // }

    private void changeActivityState(boolean state) {
        if (level == null || level.isClientSide) return;
        var oldState = level.getBlockState(worldPosition);
        if (!oldState.getValue(AltarBlock.ACTIVE).equals(state)) {
            level.setBlockAndUpdate(worldPosition, oldState.setValue(AltarBlock.ACTIVE, state));
        }
    }

    @Nullable
    public IItemHandler getCapability(@Nullable Direction ignoredSide) {
        if (!remove && recipeProgress == 0) {
            return inventory;
        }
        return null;
    }

    public AltarInventory getInventory() {
        return inventory;
    }

    public int getRecipeProgress() {
        return recipeProgress;
    }

    public void setRecipeProgress(int recipeProgress) {
        this.recipeProgress = recipeProgress;
    }

    public int getRecipeTime() {
        return recipeTime;
    }

    public void setRecipeTime(int recipeTime) {
        this.recipeTime = recipeTime;
    }

    private record EntitySacrifice(List<Entity> entities, int count) {

        private List<BlockPos> kill() {
            List<BlockPos> positions = new ArrayList<>();
            for (var i = 0; i < count; i++) {
                var entity = entities.get(i);
                entity.addTag(f("{}_sacrificed", ModConstants.MOD_ID));
                entity.kill();
                positions.add(entity.blockPosition());
            }
            return positions;
        }
    }
}

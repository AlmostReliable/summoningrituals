package com.almostreliable.summoningrituals.altar;

import com.almostreliable.summoningrituals.altar.base.TickableBlockEntity;
import com.almostreliable.summoningrituals.altar.inventory.AltarInventory;
import com.almostreliable.summoningrituals.altar.inventory.AltarInventoryHost;
import com.almostreliable.summoningrituals.compat.AltarObservable;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.data.SummoningLang.LangEntry;
import com.almostreliable.summoningrituals.network.AltarInventorySyncPacket;
import com.almostreliable.summoningrituals.network.AltarRecipeSyncPacket;
import com.almostreliable.summoningrituals.network.PacketHandler;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.RecipeInfoContainer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class AltarBlockEntity extends BlockEntity implements TickableBlockEntity, AltarInventoryHost {

    public static final AltarObservable SUMMONING_START = new AltarObservable();
    public static final AltarObservable SUMMONING_COMPLETE = new AltarObservable();
    public static final LootContextParamSet LOOT_CONTEXT_PARAM_SET = new LootContextParamSet.Builder()
        .required(LootContextParams.BLOCK_STATE)
        .required(LootContextParams.BLOCK_ENTITY)
        .required(LootContextParams.ORIGIN)
        .build();

    // TODO: implement dropping contents on destroy
    private final AltarInventory inventory;

    @Nullable
    private RecipeInfoContainer currentRecipeInfo;
    @Nullable
    private ServerPlayer invokingPlayer;
    private int recipeProgress;
    private int recipeTime;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.ALTAR_BLOCK_ENTITY.get(), pos, state);
        this.inventory = new AltarInventory(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registryAccess) {
        super.saveAdditional(tag, registryAccess);
        tag.put(Constants.INVENTORY, inventory.serializeNBT(registryAccess));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registryAccess) {
        super.loadAdditional(tag, registryAccess);
        if (tag.contains(Constants.INVENTORY)) inventory.deserializeNBT(registryAccess, tag.getCompound(Constants.INVENTORY));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryAccess) {
        return inventory.serializeWithoutInsertOrder(registryAccess);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registryAccess) {
        inventory.deserializeNBT(registryAccess, tag);
    }

    @Override
    public void tick(ServerLevel level) {
        if (currentRecipeInfo == null) return;

        if (recipeProgress >= recipeTime) {
            var recipe = currentRecipeInfo.recipe();
            if (inventory.consumeRecipeInputs(level, recipe)) {
                var itemOutputs = recipe.spawnOutputs(level, worldPosition, recipe.itemOutputs());
                var entityOutputs = recipe.spawnOutputs(level, worldPosition, recipe.entityOutputs());
                var recipeInfo = RecipeInfoContainer.outputInfo(currentRecipeInfo, itemOutputs, entityOutputs);
                SUMMONING_COMPLETE.invoke(level, worldPosition, recipeInfo, invokingPlayer);
                playOptionalPlayerSound(level, invokingPlayer, false, SoundEvents.EXPERIENCE_ORB_PICKUP);
            } else {
                sendOptionalPlayerMessage(invokingPlayer, false, SummoningLang.MISSING_INPUTS, ChatFormatting.RED);
            }

            reset(level);
            return;
        }

        if (recipeProgress == 0) {
            changeActivityState(true, level);
        }

        recipeProgress++;
        sendAltarRecipeSyncUpdate(level);
    }

    @Override
    public void onInventoryChanged(Function<HolderLookup.Provider, CompoundTag> serializer) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        setChanged();
        var inventoryTag = serializer.apply(serverLevel.registryAccess());
        PacketHandler.sendToTrackingChunk(serverLevel, worldPosition, new AltarInventorySyncPacket(worldPosition, inventoryTag));
    }

    public void removeLastInsertedItem() {
        inventory.removeLastInsertedItem();
    }

    @Override
    public void spawnItemAboveAltar(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        var itemEntity = new ItemEntity(level, 0, 0, 0, stack);
        var pos = Vec3.atCenterOf(worldPosition.above());
        itemEntity.setPos(pos);
        serverLevel.addFreshEntity(itemEntity);
    }

    @Override
    public ItemStack handleItemInsertion(@Nullable ServerPlayer player, ItemStack stack, boolean simulate) {
        if (!(level instanceof ServerLevel serverLevel)) return stack;

        if (currentRecipeInfo != null) {
            sendOptionalPlayerMessage(player, simulate, SummoningLang.IN_PROGRESS, ChatFormatting.RED);
            return stack;
        }

        if (AltarRecipe.isCatalyst(stack.getItem())) {
            var remainder = handleCatalystInsertion(serverLevel, player, stack, simulate);
            if (remainder != null) {
                return remainder;
            }
        }

        if (AltarRecipe.isInput(stack.getItem())) {
            return handleInputInsertion(serverLevel, player, stack, simulate);
        }

        return stack;
    }

    @Nullable
    private ItemStack handleCatalystInsertion(ServerLevel level, @Nullable ServerPlayer player, ItemStack stack, boolean simulate) {
        var matchingRecipes = getMatchingRecipes(level, player, stack);
        if (matchingRecipes == null) return null;

        if (matchingRecipes.size() > 1) {
            return null;
        }

        if (!simulate) inventory.setCatalyst(stack.copyWithCount(1));
        var remainder = stack.copyWithCount(stack.getCount() - 1);
        remainder = remainder.isEmpty() ? ItemStack.EMPTY : remainder;

        if (matchingRecipes.isEmpty()) {
            playOptionalPlayerSound(level, player, simulate, SoundEvents.CHAIN_BREAK);
            sendOptionalPlayerMessage(player, simulate, SummoningLang.CONDITION_FAIL, ChatFormatting.RED);
            if (simulate) return stack;
            removeLastInsertedItem();
            return remainder;
        }

        if (simulate) return remainder;

        var recipeInfo = matchingRecipes.iterator().next();
        if (!SUMMONING_START.invoke(level, worldPosition, recipeInfo, player)) {
            reset(level);
            removeLastInsertedItem();
            return remainder;
        }

        currentRecipeInfo = recipeInfo;
        invokingPlayer = player;
        recipeTime = recipeInfo.recipe().ticks();
        playOptionalPlayerSound(level, player, false, SoundEvents.BEACON_ACTIVATE);
        sendAltarRecipeSyncUpdate(level);

        return remainder;
    }

    private ItemStack handleInputInsertion(ServerLevel level, @Nullable ServerPlayer player, ItemStack stack, boolean simulate) {
        var remaining = inventory.insertInputTracked(stack, simulate);
        if (remaining.isEmpty() || stack.getCount() != remaining.getCount()) {
            playOptionalPlayerSound(level, player, simulate, SoundEvents.ITEM_PICKUP);
        }
        return remaining;
    }

    @Nullable
    private Set<RecipeInfoContainer> getMatchingRecipes(
        ServerLevel level, @Nullable ServerPlayer player, ItemStack stack
    ) {
        var recipeHolders = level.getRecipeManager().getRecipesFor(Registration.ALTAR_RECIPE_TYPE.get(), inventory, this.level);
        recipeHolders.removeIf(h -> !h.value().catalyst().test(stack));

        if (recipeHolders.isEmpty()) return null;

        var matchingRecipes = new HashSet<RecipeInfoContainer>();

        for (var recipeHolder : recipeHolders) {
            var recipe = recipeHolder.value();
            var entityInputs = recipe.entityInputs().getSacrifices(worldPosition, region -> level.getEntities(player, region));
            if (entityInputs == null) continue;
            matchingRecipes.add(RecipeInfoContainer.inputInfo(recipeHolder, entityInputs));
        }

        if (matchingRecipes.isEmpty()) {
            return null;
        }

        var lootParams = new LootParams.Builder(level)
            .withParameter(LootContextParams.BLOCK_STATE, getBlockState())
            .withParameter(LootContextParams.BLOCK_ENTITY, this)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
            .create(LOOT_CONTEXT_PARAM_SET);
        var lootContext = new LootContext.Builder(lootParams).create(Optional.empty());

        matchingRecipes.removeIf(recipeInfo -> {
            var recipe = recipeInfo.recipe();
            return !recipe.startConditions().stream().allMatch(condition -> condition.test(lootContext));
        });
        return matchingRecipes;
    }

    private void sendOptionalPlayerMessage(@Nullable ServerPlayer player, boolean simulate, LangEntry langEntry, ChatFormatting color) {
        if (player == null || simulate) return;
        player.sendSystemMessage(langEntry.get().withStyle(color));
    }

    private void playOptionalPlayerSound(ServerLevel level, @Nullable ServerPlayer player, boolean simulate, SoundEvent sound) {
        if (player == null || simulate) return;
        level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 0.5f, 1f);
    }

    private void reset(ServerLevel level) {
        currentRecipeInfo = null;
        invokingPlayer = null;
        recipeProgress = 0;
        recipeTime = 0;
        changeActivityState(false, level);
        sendAltarRecipeSyncUpdate(level);
    }

    private void sendAltarRecipeSyncUpdate(ServerLevel level) {
        PacketHandler.sendToTrackingChunk(level, worldPosition, new AltarRecipeSyncPacket(worldPosition, recipeProgress, recipeTime));
    }

    private void changeActivityState(boolean state, ServerLevel level) {
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

    @OnlyIn(Dist.CLIENT)
    public void setRecipeProgress(int recipeProgress) {
        this.recipeProgress = recipeProgress;
    }

    public int getRecipeTime() {
        return recipeTime;
    }

    @OnlyIn(Dist.CLIENT)
    public void setRecipeTime(int recipeTime) {
        this.recipeTime = recipeTime;
    }
}

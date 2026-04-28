package com.almostreliable.summoningrituals.altar;

import com.almostreliable.summoningrituals.SummoningRituals;
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
import com.almostreliable.summoningrituals.recipe.container.RecipeInfo;
import com.almostreliable.summoningrituals.recipe.container.RecipeMatch;
import com.almostreliable.summoningrituals.recipe.container.RecipeOutputs;

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
import net.minecraft.world.item.crafting.RecipeManager;
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

import com.google.common.collect.Lists;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;

public class AltarBlockEntity extends BlockEntity implements TickableBlockEntity, AltarInventoryHost {

    public static final AltarObservable SUMMONING_START = new AltarObservable();
    public static final AltarObservable SUMMONING_COMPLETE = new AltarObservable();
    public static final String SACRIFICE_TAG = SummoningRituals.getRL("marker").toString();
    private static final LootContextParamSet LOOT_CONTEXT_PARAM_SET = new LootContextParamSet.Builder()
        .required(LootContextParams.BLOCK_STATE)
        .required(LootContextParams.BLOCK_ENTITY)
        .required(LootContextParams.ORIGIN)
        .build();

    private final AltarInventory inventory;

    @Nullable
    private RecipeInfo currentRecipeInfo;
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
            var recipe = currentRecipeInfo.getRecipe();
            if (inventory.consumeRecipeInputs(level, recipe)) {
                recipe.invokeCommands(level, invokingPlayer);
                var itemOutputs = recipe.spawnOutputs(level, worldPosition, RecipeOutputs::itemOutputs);
                var entityOutputs = recipe.spawnOutputs(level, worldPosition, RecipeOutputs::entityOutputs);
                var recipeInfo = RecipeInfo.outputInfo(currentRecipeInfo, itemOutputs, entityOutputs);
                SUMMONING_COMPLETE.invoke(this, recipeInfo, invokingPlayer);
                playOptionalPlayerSound(level, invokingPlayer, false, SoundEvents.EXPERIENCE_ORB_PICKUP);
            } else {
                sendOptionalPlayerMessage(invokingPlayer, false, SummoningLang.MISSING_INPUTS, ChatFormatting.RED);
            }

            reset(level);
            return;
        }

        recipeProgress++;
        syncAltarRecipeProgress(level);
    }

    @Override
    public void onInventoryChanged(Function<HolderLookup.Provider, CompoundTag> serializer) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        setChanged();
        var inventoryTag = serializer.apply(serverLevel.registryAccess());
        PacketHandler.sendToTrackingChunk(serverLevel, worldPosition, new AltarInventorySyncPacket(worldPosition, inventoryTag));
    }

    private void removeLastInsertedItem() {
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
            sendOptionalPlayerMessage(player, simulate, SummoningLang.IN_PROGRESS, ChatFormatting.YELLOW);
            return stack;
        }

        if (player != null && player.isShiftKeyDown()) {
            removeLastInsertedItem();
            return stack;
        }

        RecipeMatch matchResult = null;
        if (AltarRecipe.isInitiator(serverLevel.getRecipeManager(), stack.getItem())) {
            matchResult = handleInitiatorInsertion(serverLevel, player, stack, simulate);
            if (matchResult.getInteractionRemainder() != null) {
                return matchResult.getInteractionRemainder();
            }
        }

        if (AltarRecipe.isInput(serverLevel.getRecipeManager(), stack.getItem())) {
            return handleInputInsertion(serverLevel, player, stack, simulate);
        }

        if (!simulate && matchResult != null && matchResult.hasIssue()) {
            // if this is true, the item wasn't an input and the initiator insertion failed
            playOptionalPlayerSound(serverLevel, player, false, SoundEvents.CHAIN_BREAK);
            sendOptionalPlayerMessage(player, false, matchResult.getMatchIssue().getIssueMessage(), ChatFormatting.RED);
        }

        return stack;
    }

    private RecipeMatch handleInitiatorInsertion(
        ServerLevel level, @Nullable ServerPlayer player, ItemStack stack, boolean simulate
    ) {
        var recipeMatch = getMatchingRecipes(level, player, stack);
        if (recipeMatch.hasIssue()) return recipeMatch;

        if (!simulate) inventory.setInitiator(stack.copyWithCount(1));
        var remainder = stack.copyWithCount(stack.getCount() - 1);
        remainder = remainder.isEmpty() ? ItemStack.EMPTY : remainder;
        recipeMatch.setInteractionRemainder(remainder);

        if (simulate) return recipeMatch;

        var recipeInfo = recipeMatch.getRecipeInfo();
        if (!SUMMONING_START.invoke(this, recipeInfo, player)) {
            reset(level);
            removeLastInsertedItem();
            return recipeMatch;
        }

        for (var entityInput : recipeInfo.getInputEntities()) {
            entityInput.addTag(SACRIFICE_TAG);
            entityInput.kill();
        }

        currentRecipeInfo = recipeInfo;
        invokingPlayer = player;
        recipeTime = recipeInfo.getRecipe().ticks();
        playOptionalPlayerSound(level, player, false, SoundEvents.BEACON_ACTIVATE);
        changeActivityState(true, level);
        syncAltarRecipeStart(level);

        return recipeMatch;
    }

    private ItemStack handleInputInsertion(ServerLevel level, @Nullable ServerPlayer player, ItemStack stack, boolean simulate) {
        var remaining = inventory.insertInputTracked(stack, simulate);
        if (remaining.isEmpty() || stack.getCount() != remaining.getCount()) {
            playOptionalPlayerSound(level, player, simulate, SoundEvents.ITEM_PICKUP);
        }
        return remaining;
    }

    private RecipeMatch getMatchingRecipes(ServerLevel level, @Nullable ServerPlayer player, ItemStack stack) {
        var altarRecipes = level.getRecipeManager().getRecipesFor(Registration.ALTAR_RECIPE_TYPE.get(), inventory, level);
        var recipeHolders = Lists.newArrayList(altarRecipes); // create mutable copy
        recipeHolders.removeIf(h -> !h.value().initiator().test(stack));
        if (recipeHolders.isEmpty()) return RecipeMatch.INVALID_INITIATOR;

        var lootParams = new LootParams.Builder(level)
            .withParameter(LootContextParams.BLOCK_STATE, getBlockState())
            .withParameter(LootContextParams.BLOCK_ENTITY, this)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
            .create(LOOT_CONTEXT_PARAM_SET);
        var lootContext = new LootContext.Builder(lootParams).create(Optional.empty());

        var matchingRecipes = new HashSet<RecipeInfo>();
        for (var recipeHolder : recipeHolders) {
            var recipeId = recipeHolder.id();
            var recipe = recipeHolder.value();
            var entityInputs = recipe.getSacrifices(worldPosition, recipeId, region -> level.getEntities(player, region));
            if (entityInputs == null) continue;

            var recipeInfo = RecipeInfo.inputInfo(recipeHolder, entityInputs);

            var blockPatternExtension = recipe.blockPatternExtension();
            if (blockPatternExtension.isPresent()) {
                var blockPattern = blockPatternExtension.get();
                var blockPatternMatch = blockPattern.test(lootContext, false);
                recipeInfo = RecipeInfo.blockPatternInfo(recipeInfo, blockPatternMatch);
            }

            matchingRecipes.add(recipeInfo);
        }
        if (matchingRecipes.isEmpty()) return RecipeMatch.MISSING_SACRIFICES;

        matchingRecipes.removeIf(recipeInfo -> {
            var recipe = recipeInfo.getRecipe();
            return !recipe.conditions().stream().allMatch(condition -> condition.test(lootContext));
        });
        if (matchingRecipes.isEmpty()) return RecipeMatch.FAILED_CONDITIONS;

        matchingRecipes.removeIf(recipeInfo -> {
            var recipe = recipeInfo.getRecipe();
            var blockPattern = recipe.blockPattern();
            var recipes = matchingRecipes.size();
            return blockPattern.filter(p -> !p.test(lootContext, recipes == 1)).isPresent();
        });
        if (matchingRecipes.isEmpty()) return RecipeMatch.WRONG_PATTERN;

        if (matchingRecipes.size() > 1) return RecipeMatch.MULTI_MATCH;

        return RecipeMatch.of(matchingRecipes);
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
        syncAltarRecipeProgress(level);
    }

    private void syncAltarRecipeStart(ServerLevel level) {
        PacketHandler.sendToTrackingChunk(
            level,
            worldPosition,
            new AltarRecipeSyncPacket(worldPosition, Optional.ofNullable(currentRecipeInfo), recipeProgress, recipeTime)
        );
    }

    private void syncAltarRecipeProgress(ServerLevel level) {
        PacketHandler.sendToTrackingChunk(
            level,
            worldPosition,
            new AltarRecipeSyncPacket(worldPosition, Optional.empty(), recipeProgress, recipeTime)
        );
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

    @Nullable
    @Override
    public RecipeManager getRecipeManager() {
        if (level == null) return null;
        return level.getRecipeManager();
    }

    public AltarInventory getInventory() {
        return inventory;
    }

    @Nullable
    public RecipeInfo getCurrentRecipeInfo() {
        return currentRecipeInfo;
    }

    public void setCurrentRecipeInfo(@Nullable RecipeInfo currentRecipeInfo) {
        this.currentRecipeInfo = currentRecipeInfo;
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

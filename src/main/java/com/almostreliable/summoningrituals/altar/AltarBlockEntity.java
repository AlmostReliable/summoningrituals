package com.almostreliable.summoningrituals.altar;

import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.altar.base.TickableBlockEntity;
import com.almostreliable.summoningrituals.altar.inventory.AltarInventory;
import com.almostreliable.summoningrituals.altar.inventory.AltarInventoryHost;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.data.SummoningLang.LangEntry;
import com.almostreliable.summoningrituals.network.AltarInventorySyncPacket;
import com.almostreliable.summoningrituals.network.AltarRecipeSyncPacket;
import com.almostreliable.summoningrituals.network.PacketHandler;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.util.GameUtils;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
        if (currentRecipe == null && !inventory.getCatalyst().isEmpty()) {
            var recipe = findRecipe();
            if (recipe == null) {
                resetSummoning(level, true);
                return;
            }
            handleSummoning(recipe, null);
        }
        if (currentRecipe == null) return;

        if (recipeProgress >= currentRecipe.ticks()) {
            if (inventory.handleRecipe(currentRecipe)) {
                // currentRecipe.outputs().handleRecipe((ServerLevel) level, worldPosition);
                SUMMONING_COMPLETE.invoke(level, worldPosition, currentRecipe, invokingPlayer);
                playOptionalPlayerSound(level, invokingPlayer, false, SoundEvents.EXPERIENCE_ORB_PICKUP);
                resetSummoning(level, false);
            } else {
                GameUtils.sendPlayerMessage(invokingPlayer, Constants.INVALID, ChatFormatting.RED);
                resetSummoning(level, true);
            }
            return;
        }

        if (recipeProgress == 0) {
            changeActivityState(true);
            if (sacrifices != null && !sacrifices.isEmpty()) {
                for (var sacrifice : sacrifices) {
                    sacrifice.kill();
                }
            }
        }
        recipeProgress++;
        sendAltarRecipeSyncUpdate(level);
    }

    private void sendOptionalPlayerMessage(@Nullable ServerPlayer player, boolean simulate, LangEntry langEntry, ChatFormatting color) {
        if (player == null || simulate) return;
        player.sendSystemMessage(langEntry.get().withStyle(color));
    }

    private void playOptionalPlayerSound(ServerLevel level, @Nullable ServerPlayer player, boolean simulate, SoundEvent sound) {
        if (player == null || simulate) return;
        level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 0.5f, 1f);
    }

    @Nullable
    private ItemStack handleCatalystInsertion(ServerLevel level, @Nullable ServerPlayer player, ItemStack stack, boolean simulate) {
        var recipeHolders = level.getRecipeManager().getRecipesFor(Registration.ALTAR_RECIPE_TYPE.get(), inventory, this.level);
        recipeHolders.removeIf(h -> h.value().catalyst().test(stack));

        if (recipeHolders.isEmpty()) return null;

        var validRecipes = new HashMap<RecipeHolder<AltarRecipe>, List<Entity>>();

        var lootParams = new LootParams.Builder(level)
            .withParameter(LootContextParams.BLOCK_STATE, getBlockState())
            .withParameter(LootContextParams.BLOCK_ENTITY, this)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
            .create(LOOT_CONTEXT_PARAM_SET);
        var lootContext = new LootContext.Builder(lootParams).create(Optional.empty());

        for (var recipeHolder : recipeHolders) {
            var recipe = recipeHolder.value();
            var entityInputs = recipe.entityInputs().getSacrifices(worldPosition, region -> level.getEntities(player, region));
            if (entityInputs == null) continue;
            validRecipes.put(recipeHolder, entityInputs);
        }

        if (validRecipes.isEmpty()) {
            return null;
        }

        validRecipes.keySet().removeIf(recipeHolder -> {
            var recipe = recipeHolder.value();
            return !recipe.startConditions().stream().allMatch(condition -> condition.test(lootContext));
        });

        if (validRecipes.isEmpty()) {
            playOptionalPlayerSound(level, player, simulate, SoundEvents.CHAIN_BREAK);
            return stack;
        }

        if (validRecipes.size() != 1) {
            return null;
        }

        var entry = validRecipes.entrySet().iterator().next();
        var recipe = entry.getKey().value();
        var entities = entry.getValue(); // TODO: pass entities to event
        if (!SUMMONING_START.invoke(level, worldPosition, recipe, player)) {
            resetSummoning(level, true);
            return stack;
        }

        currentRecipe = recipe;
        invokingPlayer = player;
        recipeTime = recipe.ticks();
        playOptionalPlayerSound(level, player, simulate, SoundEvents.BEACON_ACTIVATE);
        sendAltarRecipeSyncUpdate(level);

        var remainder = stack.copyWithCount(stack.getCount() - 1);
        return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
    }

    private ItemStack handleInputInsertion(ServerLevel level, @Nullable ServerPlayer player, ItemStack stack, boolean simulate) {

        var remaining = inventory.insertInputTracked(stack);
        if (player != null && (remaining.isEmpty() || stack.getCount() != remaining.getCount())) {
            playOptionalPlayerSound(level, player, simulate, SoundEvents.ITEM_PICKUP);
        }
        return remaining;
    }

    /*
    needs to return remainder, should not modify initial stack because it's called from inventory
     */
    @Override
    public ItemStack handleItemInsertion(@Nullable ServerPlayer player, ItemStack stack, boolean simulate) {
        if (!(level instanceof ServerLevel serverLevel)) return stack;

        if (currentRecipe != null) {
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

    @Override
    public void spawnItemAboveAltar(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        var itemEntity = new ItemEntity(level, 0, 0, 0, stack);
        var pos = Vec3.atCenterOf(worldPosition.above());
        itemEntity.setPos(pos);
        serverLevel.addFreshEntity(itemEntity);
    }

    public void removeLastInsertedItem() {
        inventory.removeLastInsertedItem();
    }

    private void resetSummoning(ServerLevel level, boolean removeLastInsertedItem) {
        assert this.level != null;
        currentRecipe = null;
        sacrifices = null;
        invokingPlayer = null;
        recipeProgress = 0;
        recipeTime = 0;
        sendAltarRecipeSyncUpdate(level);
        changeActivityState(false);
        if (removeLastInsertedItem) removeLastInsertedItem();
    }

    private void sendAltarRecipeSyncUpdate(ServerLevel level) {
        PacketHandler.sendToTrackingChunk(level, worldPosition, new AltarRecipeSyncPacket(worldPosition, recipeProgress, recipeTime));
    }

    private void changeActivityState(boolean state) {
        if (level == null || level.isClientSide) return;
        var oldState = level.getBlockState(worldPosition);
        if (!oldState.getValue(AltarBlock.ACTIVE).equals(state)) {
            level.setBlockAndUpdate(worldPosition, oldState.setValue(AltarBlock.ACTIVE, state));
        }
    }

    @Override
    public void onInventoryChanged(Function<HolderLookup.Provider, CompoundTag> serializer) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        setChanged();
        var inventoryTag = serializer.apply(serverLevel.registryAccess());
        PacketHandler.sendToTrackingChunk(serverLevel, worldPosition, new AltarInventorySyncPacket(worldPosition, inventoryTag));
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
            var positions = new ArrayList<BlockPos>();
            for (var i = 0; i < count; i++) {
                var entity = entities.get(i);
                entity.addTag(ModConstants.MOD_ID + "_sacrificed");
                entity.kill();
                positions.add(entity.blockPosition());
            }
            return positions;
        }
    }
}

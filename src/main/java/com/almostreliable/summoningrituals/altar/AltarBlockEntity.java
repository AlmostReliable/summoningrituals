package com.almostreliable.summoningrituals.altar;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.Registration;
import com.almostreliable.summoningrituals.SummoningRitualsConstants;
import com.almostreliable.summoningrituals.platform.Platform;
import com.almostreliable.summoningrituals.platform.PlatformBlockEntity;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.recipe.component.RecipeSacrifices;
import com.almostreliable.summoningrituals.util.GameUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AltarBlockEntity extends PlatformBlockEntity {

    public static final AltarObservable SUMMONING_START = new AltarObservable();
    public static final AltarObservable SUMMONING_COMPLETE = new AltarObservable();

    @Nullable private AltarRecipe currentRecipe;
    @Nullable private List<EntitySacrifice> sacrifices;
    @Nullable private ServerPlayer invokingPlayer;
    private int processTime;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.ALTAR_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(Constants.INVENTORY)) inventory.deserialize(tag.getCompound(Constants.INVENTORY));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(Constants.INVENTORY, inventory.serialize());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        var tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ItemStack handleInteraction(@Nullable ServerPlayer player, ItemStack stack) {
        if (progress > 0) {
            GameUtils.sendPlayerMessage(player, Constants.PROGRESS, ChatFormatting.RED);
            return stack;
        }

        if (stack.isEmpty()) {
            if (player != null && player.isShiftKeyDown()) {
                inventory.popLastInserted();
            }
            return ItemStack.EMPTY;
        }

        if (AltarRecipe.CATALYST_CACHE.stream().anyMatch(ingredient -> ingredient.test(stack))) {
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

    void playerDestroy(boolean creative) {
        assert level != null && !level.isClientSide;
        inventory.dropContents();
        if (creative) return;
        GameUtils.dropItem(level, worldPosition, new ItemStack(Registration.ALTAR_ITEM.get()), true);
    }

    void tick() {
        if (level == null) return;

        if (!inventory.getCatalyst().isEmpty() && currentRecipe == null) {
            var recipe = findRecipe();
            if (recipe == null) {
                resetSummoning(true);
                return;
            }
            handleSummoning(recipe, null);
        }
        if (currentRecipe == null) return;

        if (progress >= currentRecipe.recipeTime()) {
            if (inventory.handleRecipe(currentRecipe)) {
                currentRecipe.outputs().handleRecipe((ServerLevel) level, worldPosition);
                SUMMONING_COMPLETE.invoke((ServerLevel) level, worldPosition, currentRecipe, invokingPlayer);
                GameUtils.playSound(level, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP);
                resetSummoning(false);
            } else {
                GameUtils.sendPlayerMessage(invokingPlayer, Constants.INVALID, ChatFormatting.RED);
                resetSummoning(true);
            }
            return;
        }

        if (progress == 0) {
            changeActivityState(true);
            if (sacrifices != null && !sacrifices.isEmpty()) {
                sacrifices.stream()
                    .map(EntitySacrifice::kill)
                    .filter(positions -> !positions.isEmpty())
                    .forEach(p -> Platform.sendParticleEmit(level, p));
            }
        }
        progress++;
        Platform.sendProgressUpdate(level, worldPosition, progress);
    }

    private void resetSummoning(boolean popLastInserted) {
        assert level != null;
        currentRecipe = null;
        sacrifices = null;
        invokingPlayer = null;
        progress = 0;
        Platform.sendProgressUpdate(level, worldPosition, progress);
        processTime = 0;
        Platform.sendProcessTimeUpdate(level, worldPosition, processTime);
        changeActivityState(false);
        if (popLastInserted) inventory.popLastInserted();
    }

    private void handleSummoning(AltarRecipe recipe, @Nullable ServerPlayer player) {
        assert level != null && !level.isClientSide;

        sacrifices = checkSacrifices(recipe.sacrifices(), player);
        if (sacrifices == null ||
            !checkBlockBelow(recipe.blockBelow(), player) ||
            !recipe.dayTime().check(level, player) ||
            !recipe.weather().check(level, player)) {
            inventory.popLastInserted();
            GameUtils.playSound(level, worldPosition, SoundEvents.CHAIN_BREAK);
            return;
        }

        if (!SUMMONING_START.invoke((ServerLevel) level, worldPosition, recipe, player)) {
            resetSummoning(true);
            return;
        }
        currentRecipe = recipe;
        invokingPlayer = player;
        processTime = recipe.recipeTime();
        GameUtils.playSound(level, worldPosition, SoundEvents.BEACON_ACTIVATE);
        Platform.sendProcessTimeUpdate(level, worldPosition, processTime);
    }

    @Nullable
    private AltarRecipe findRecipe() {
        assert level != null && !level.isClientSide;
        var recipeManager = level.getRecipeManager();
        return recipeManager.getRecipeFor(Registration.ALTAR_RECIPE.type().get(), inventory.getVanillaInv(), level)
            .orElse(null);
    }

    @Nullable
    private List<EntitySacrifice> checkSacrifices(RecipeSacrifices sacrifices, @Nullable ServerPlayer player) {
        assert level != null && !level.isClientSide;
        if (sacrifices.isEmpty()) return List.of();
        var region = sacrifices.getRegion(worldPosition);
        var entities = level.getEntities(player, region);
        List<EntitySacrifice> toKill = new ArrayList<>();
        var success = sacrifices.test(sacrifice -> {
            var found = entities.stream().filter(sacrifice).toList();
            if (found.size() < sacrifice.count()) {
                GameUtils.sendPlayerMessage(player, Constants.SACRIFICES, ChatFormatting.YELLOW);
                return false;
            }
            toKill.add(new EntitySacrifice(found, sacrifice.count()));
            return true;
        });
        return success ? toKill : null;
    }

    private boolean checkBlockBelow(@Nullable BlockReference blockBelow, @Nullable ServerPlayer player) {
        assert level != null && !level.isClientSide;
        if (blockBelow == null || blockBelow.test(level.getBlockState(worldPosition.below()))) {
            return true;
        }
        GameUtils.sendPlayerMessage(player, Constants.BLOCK_BELOW, ChatFormatting.YELLOW);
        return false;
    }

    private void changeActivityState(boolean state) {
        if (level == null || level.isClientSide) return;
        var oldState = level.getBlockState(worldPosition);
        if (!oldState.getValue(AltarBlock.ACTIVE).equals(state)) {
            level.setBlockAndUpdate(worldPosition, oldState.setValue(AltarBlock.ACTIVE, state));
        }
    }

    public int getProcessTime() {
        return processTime;
    }

    public void setProcessTime(int processTime) {
        this.processTime = processTime;
    }

    private record EntitySacrifice(List<Entity> entities, int count) {
        private List<BlockPos> kill() {
            List<BlockPos> positions = new ArrayList<>();
            for (var i = 0; i < count; i++) {
                var entity = entities.get(i);
                entity.addTag(f("{}_sacrificed", SummoningRitualsConstants.MOD_ID));
                entity.kill();
                positions.add(entity.blockPosition());
            }
            return positions;
        }
    }
}

package com.almostreliable.summoningrituals.altar;

import com.almostreliable.summoningrituals.BuildConfig;
import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.Setup;
import com.almostreliable.summoningrituals.inventory.AltarInventory;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.BlockReference;
import com.almostreliable.summoningrituals.recipe.RecipeSacrifices;
import com.almostreliable.summoningrituals.util.GameUtils;
import com.almostreliable.summoningrituals.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class AltarEntity extends BlockEntity {

    public final AltarInventory inventory;
    private final LazyOptional<AltarInventory> inventoryCap;

    @Nullable private AltarRecipe recipeCache;
    @Nullable private AltarRecipe currentRecipe;
    @Nullable private List<EntitySacrifice> sacrifices;
    @Nullable private ServerPlayer invokingPlayer;
    private int progress;

    public AltarEntity(BlockPos pos, BlockState state) {
        super(Setup.ALTAR_ENTITY.get(), pos, state);
        inventory = new AltarInventory(this);
        inventoryCap = LazyOptional.of(() -> inventory);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(Constants.INVENTORY)) inventory.deserializeNBT(tag.getCompound(Constants.INVENTORY));
        if (tag.contains(Constants.PROGRESS)) progress = tag.getInt(Constants.PROGRESS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(Constants.INVENTORY, inventory.serializeNBT());
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

    public void playerDestroy(boolean creative) {
        assert level != null && !level.isClientSide;
        inventory.dropContents();
        if (creative) return;
        GameUtils.dropItem(level, worldPosition, new ItemStack(Setup.ALTAR_ITEM.get()), true);
    }

    public InteractionResult handleInteraction(ServerPlayer player, ItemStack stack) {
        if (progress > 0) {
            TextUtils.sendPlayerMessage(player, "in_progress", ChatFormatting.RED);
            return InteractionResult.PASS;
        }

        if (stack.isEmpty()) {
            if (player.isShiftKeyDown()) {
                inventory.popLastInserted();
            }
            return InteractionResult.SUCCESS;
        }

        if (AltarRecipe.CATALYST_CACHE.stream().anyMatch(ingredient -> ingredient.test(stack))) {
            inventory.setCatalyst(new ItemStack(stack.getItem(), 1));
            var recipe = findRecipe();
            if (recipe == null) {
                inventory.setCatalyst(ItemStack.EMPTY);
            } else {
                recipeCache = recipe;
                stack.shrink(1);
                player.setItemInHand(InteractionHand.MAIN_HAND, stack.isEmpty() ? ItemStack.EMPTY : stack);
                handleSummoning(recipe, player);
                return InteractionResult.SUCCESS;
            }
        }

        var remaining = inventory.insertItem(stack);
        player.setItemInHand(InteractionHand.MAIN_HAND, remaining);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryCap.invalidate();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (!remove && cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) && progress == 0) {
            return inventoryCap.cast();
        }
        return super.getCapability(cap, side);
    }

    public void sendUpdate() {
        if (level == null || level.isClientSide) return;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 1 | 2);
    }

    public void tick() {
        if (level == null || currentRecipe == null) return;

        if (progress >= currentRecipe.getRecipeTime()) {
            if (inventory.handleRecipe(currentRecipe)) {
                // TODO: implement proper result handling, not only item stack output
                GameUtils.dropItem(level, worldPosition, (ItemStack) currentRecipe.getOutput().getEntry(), true);
            } else {
                inventory.popLastInserted();
                if (invokingPlayer != null) {
                    TextUtils.sendPlayerMessage(invokingPlayer, "no_output", ChatFormatting.RED);
                }
            }
            currentRecipe = null;
            sacrifices = null;
            invokingPlayer = null;
            progress = 0;
            changeActivityState(false);
            return;
        }

        if (progress == 0) {
            changeActivityState(true);
            if (sacrifices != null && !sacrifices.isEmpty()) {
                sacrifices.forEach(EntitySacrifice::kill);
            }
        }
        progress++;
    }

    private void handleSummoning(AltarRecipe recipe, ServerPlayer player) {
        assert level != null && !level.isClientSide;

        sacrifices = checkSacrifices(recipe.getSacrifices(), player);
        if (sacrifices == null ||
            !checkBlockBelow(recipe.getBlockBelow(), player) ||
            !recipe.getDayTime().check(level, player) ||
            !recipe.getWeather().check(level, player)) {
            inventory.popLastInserted();
            return;
        }

        currentRecipe = recipe;
        invokingPlayer = player;
    }

    @Nullable
    private AltarRecipe findRecipe() {
        assert level != null && !level.isClientSide;
        if (recipeCache != null && recipeCache.matches(inventory.getVanillaInv(), level)) {
            return recipeCache;
        }
        var recipeManager = GameUtils.getRecipeManager(level);
        return recipeManager.getRecipeFor(Setup.ALTAR_RECIPE.type().get(), inventory.getVanillaInv(), level)
            .orElse(null);
    }

    @Nullable
    private List<EntitySacrifice> checkSacrifices(@Nullable RecipeSacrifices sacrifices, ServerPlayer player) {
        assert level != null && !level.isClientSide;
        if (sacrifices == null) return null;
        var region = sacrifices.getRegion(worldPosition);
        var entities = level.getEntities(player, region);
        List<EntitySacrifice> toKill = new ArrayList<>();
        var success = sacrifices.test(sacrifice -> {
            var found = entities.stream().filter(sacrifice::matches).toList();
            if (found.size() < sacrifice.count()) {
                TextUtils.sendPlayerMessage(player, "sacrifice_missing", ChatFormatting.YELLOW);
                return false;
            }
            toKill.add(new EntitySacrifice(found, sacrifice.count()));
            return true;
        });
        return success ? toKill : null;
    }

    private boolean checkBlockBelow(@Nullable BlockReference blockBelow, ServerPlayer player) {
        assert level != null && !level.isClientSide;
        if (blockBelow == null || blockBelow.test(level.getBlockState(worldPosition.below()))) {
            return true;
        }
        TextUtils.sendPlayerMessage(player, "no_block_below", ChatFormatting.YELLOW);
        return false;
    }

    private void changeActivityState(boolean state) {
        if (level == null || level.isClientSide) return;
        var oldState = level.getBlockState(worldPosition);
        if (!oldState.getValue(AltarBlock.ACTIVE).equals(state)) {
            level.setBlockAndUpdate(worldPosition, oldState.setValue(AltarBlock.ACTIVE, state));
        }
    }

    private record EntitySacrifice(List<Entity> entities, int count) {
        private void kill() {
            for (var i = 0; i < count; i++) {
                var entity = entities.get(i);
                entity.addTag(f("{}_sacrificed", BuildConfig.MOD_ID));
                entity.kill();
            }
        }
    }
}

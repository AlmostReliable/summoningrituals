package com.almostreliable.summoningrituals.altar;

import com.almostreliable.summoningrituals.BuildConfig;
import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.Setup;
import com.almostreliable.summoningrituals.inventory.AltarInventory;
import com.almostreliable.summoningrituals.network.IPacket;
import com.almostreliable.summoningrituals.network.PacketHandler;
import com.almostreliable.summoningrituals.network.packet.ProgressUpdatePacket;
import com.almostreliable.summoningrituals.network.packet.SacrificeParticlePacket;
import com.almostreliable.summoningrituals.recipe.AltarRecipe;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.recipe.component.RecipeSacrifices;
import com.almostreliable.summoningrituals.util.GameUtils;
import com.almostreliable.summoningrituals.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.almostreliable.summoningrituals.util.TextUtils.f;

public class AltarEntity extends BlockEntity {

    final AltarInventory inventory;
    private final LazyOptional<AltarInventory> inventoryCap;

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
        if (progress > 0) tag.putInt(Constants.PROGRESS, progress);
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

    public ItemStack handleInteraction(@Nullable ServerPlayer player, ItemStack stack) {
        if (progress > 0) {
            TextUtils.sendPlayerMessage(player, "in_progress", ChatFormatting.RED);
            return stack;
        }

        if (stack.isEmpty()) {
            if (player != null && player.isShiftKeyDown()) {
                inventory.popLastInserted();
            }
            return ItemStack.EMPTY;
        }

        if (AltarRecipe.CATALYST_CACHE.stream().anyMatch(ingredient -> ingredient.test(stack))) {
            inventory.setCatalyst(new ItemStack(stack.getItem(), 1));
            var recipe = findRecipe();
            if (recipe == null) {
                inventory.setCatalyst(ItemStack.EMPTY);
            } else {
                stack.shrink(1);
                var result = stack.isEmpty() ? ItemStack.EMPTY : stack;
                if (player != null) player.setItemInHand(InteractionHand.MAIN_HAND, result);
                handleSummoning(recipe, player);
                return result;
            }
        }

        var remaining = inventory.handleInsertion(stack);
        if (player != null) player.setItemInHand(InteractionHand.MAIN_HAND, remaining);
        return remaining;
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

    void playerDestroy(boolean creative) {
        assert level != null && !level.isClientSide;
        inventory.dropContents();
        if (creative) return;
        GameUtils.dropItem(level, worldPosition, new ItemStack(Setup.ALTAR_ITEM.get()), true);
    }

    void tick() {
        if (level == null) return;

        if (progress > 0 && currentRecipe == null) {
            progress = 0;
            var recipe = findRecipe();
            if (recipe == null) return;
            handleSummoning(recipe, null);
        }
        if (currentRecipe == null) return;

        if (progress >= currentRecipe.getRecipeTime()) {
            if (inventory.handleRecipe(currentRecipe)) {
                currentRecipe.getOutputs().handleRecipe((ServerLevel) level, worldPosition);
            } else {
                inventory.popLastInserted();
                TextUtils.sendPlayerMessage(invokingPlayer, "no_output", ChatFormatting.RED);
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
                sacrifices.stream()
                    .map(EntitySacrifice::kill)
                    .filter(positions -> !positions.isEmpty())
                    .forEach(positions -> trackingChunkPacket(new SacrificeParticlePacket(positions)));
            }
        }
        progress++;
        trackingChunkPacket(new ProgressUpdatePacket(worldPosition, progress));
    }

    private void trackingChunkPacket(IPacket<?> packet) {
        if (level == null || level.isClientSide) return;
        PacketHandler.CHANNEL.send(
            PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)),
            packet
        );
    }

    private void handleSummoning(AltarRecipe recipe, @Nullable ServerPlayer player) {
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
        var recipeManager = GameUtils.getRecipeManager(level);
        return recipeManager.getRecipeFor(Setup.ALTAR_RECIPE.type().get(), inventory.getVanillaInv(), level)
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
                TextUtils.sendPlayerMessage(player, "sacrifice_missing", ChatFormatting.YELLOW);
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

    public void setProgress(int progress) {
        this.progress = progress;
        setChanged();
    }

    private record EntitySacrifice(List<Entity> entities, int count) {
        private List<BlockPos> kill() {
            List<BlockPos> positions = new ArrayList<>();
            for (var i = 0; i < count; i++) {
                var entity = entities.get(i);
                entity.addTag(f("{}_sacrificed", BuildConfig.MOD_ID));
                entity.kill();
                positions.add(entity.blockPosition());
            }
            return positions;
        }
    }
}

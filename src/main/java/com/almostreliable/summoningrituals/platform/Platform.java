package com.almostreliable.summoningrituals.platform;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.network.ClientAltarUpdatePacket;
import com.almostreliable.summoningrituals.network.PacketHandler;
import com.almostreliable.summoningrituals.network.SacrificeParticlePacket;
import com.almostreliable.summoningrituals.network.ServerToClientPacket;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.util.SerializeUtils;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

public final class Platform {

    private Platform() {}

    public static void sendProgressUpdate(Level level, BlockPos pos, int progress) {
        sendPacket(level, pos, ClientAltarUpdatePacket.progressUpdate(pos, progress));
    }

    public static void sendProcessTimeUpdate(Level level, BlockPos pos, int processTime) {
        sendPacket(level, pos, ClientAltarUpdatePacket.processTimeUpdate(pos, processTime));
    }

    public static void sendParticleEmit(Level level, List<BlockPos> positions) {
        sendPacket(level, positions.get(0), new SacrificeParticlePacket(positions));
    }

    private static void sendPacket(Level level, BlockPos pos, ServerToClientPacket<?> packet) {
        PacketHandler.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
    }

    public static CompoundTag serializeItemStack(ItemStack stack) {
        return stack.serializeNBT();
    }

    public static CompoundTag serializeEntity(Entity entity) {
        return entity.serializeNBT();
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderSingleBlock(
        BlockRenderDispatcher blockRenderer, BlockReference blockReference, PoseStack stack,
        MultiBufferSource.BufferSource bufferSource
    ) {
        // noinspection DataFlowIssue
        blockRenderer.renderSingleBlock(
            blockReference.getDisplayState(),
            stack,
            bufferSource,
            LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY,
            ModelData.EMPTY,
            null
        );
    }

    public static EntityType<?> mobFromId(@Nullable ResourceLocation id) {
        return SerializeUtils.getFromRegistry(ForgeRegistries.ENTITY_TYPES, id);
    }

    public static EntityType<?> mobFromJson(JsonObject json) {
        var id = new ResourceLocation(GsonHelper.getAsString(json, Constants.MOB));
        return mobFromId(id);
    }

    public static ItemStack itemStackFromJson(JsonObject json) {
        return CraftingHelper.getItemStack(json, true, true);
    }

    public static ResourceLocation getId(Item item) {
        var id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) throw new IllegalArgumentException("Item " + item + " is not registered");
        return id;
    }

    public static ResourceLocation getId(Block block) {
        var id = ForgeRegistries.BLOCKS.getKey(block);
        if (id == null) throw new IllegalArgumentException("Block " + block + " is not registered");
        return id;
    }

    public static ResourceLocation getId(EntityType<?> entityType) {
        var id = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        if (id == null) throw new IllegalArgumentException("Entity type " + entityType + " is not registered");
        return id;
    }

    public static Stream<? extends TagKey<?>> getTagsFor(EntityType<?> entityType) {
        return ForgeRegistries.ENTITY_TYPES.getHolder(
            ResourceKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), getId(entityType))
        ).map(Holder::tags).orElseGet(Stream::empty);
    }

    public static Stream<? extends TagKey<?>> getTagsFor(Block block) {
        return ForgeRegistries.BLOCKS.getHolder(
            ResourceKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), getId(block))
        ).map(Holder::tags).orElseGet(Stream::empty);
    }
}

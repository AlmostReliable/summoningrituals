package com.almostreliable.summoningrituals.platform;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.Registration;
import com.almostreliable.summoningrituals.network.ClientAltarUpdatePacket;
import com.almostreliable.summoningrituals.network.SacrificeParticlePacket;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.util.SerializeUtils;
import com.almostreliable.summoningrituals.util.Utils;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

public final class Platform {

    private Platform() {}

    public static CreativeModeTab createTab() {
        return FabricItemGroupBuilder.build(
            Utils.getRL("tab"), () -> Registration.ALTAR_ITEM.get().defaultInstance
        );
    }

    public static <T extends BlockEntity> void registerBlockEntityRenderer(
        BlockEntityType<T> blockEntityType, BlockEntityRendererProvider<T> renderer
    ) {
        BlockEntityRendererRegistry.register(blockEntityType, renderer);
    }

    public static void sendProgressUpdate(BlockPos pos, int progress) {
        ClientAltarUpdatePacket.progressUpdate(pos, progress);
    }

    public static void sendProcessTimeUpdate(BlockPos pos, int processTime) {
        ClientAltarUpdatePacket.processTimeUpdate(pos, processTime);
    }

    public static void sendParticleEmit(List<BlockPos> positions) {
        ClientPlayNetworking.send(SacrificeParticlePacket.CHANNEL, SacrificeParticlePacket.encode(positions));
    }

    public static CompoundTag serializeItemStack(ItemStack stack) {
        var tag = new CompoundTag();
        stack.save(tag);
        return tag;
    }

    public static CompoundTag serializeEntity(Entity entity) {
        var tag = new CompoundTag();
        var id = entity.encodeId;
        if (id != null) tag.putString("id", id);
        return entity.saveWithoutId(tag);
    }

    @Environment(EnvType.CLIENT)
    public static void renderSingleBlock(
        BlockRenderDispatcher blockRenderer, BlockReference blockReference, PoseStack stack,
        MultiBufferSource.BufferSource bufferSource
    ) {
        blockRenderer.renderSingleBlock(
            blockReference.displayState,
            stack,
            bufferSource,
            LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY
        );
    }

    public static EntityType<?> mobFromId(@Nullable ResourceLocation id) {
        return SerializeUtils.getFromRegistry(Registry.ENTITY_TYPE, id);
    }

    public static EntityType<?> mobFromJson(JsonObject json) {
        var id = new ResourceLocation(GsonHelper.getAsString(json, Constants.MOB));
        return mobFromId(id);
    }

    public static ResourceLocation getId(Item item) {
        return Registry.ITEM.getKey(item);
    }

    public static ResourceLocation getId(Block block) {
        return Registry.BLOCK.getKey(block);
    }

    public static ResourceLocation getId(EntityType<?> entityType) {
        return Registry.ENTITY_TYPE.getKey(entityType);
    }

    public static Stream<? extends TagKey<?>> getTagsFor(EntityType<?> entityType) {
        return Registry.ENTITY_TYPE.getHolder(
            ResourceKey.create(Registry.ENTITY_TYPE.key(), getId(entityType))
        ).map(Holder::tags).orElseGet(Stream::empty);
    }

    public static Stream<? extends TagKey<?>> getTagsFor(Block block) {
        return Registry.BLOCK.getHolder(
            ResourceKey.create(Registry.BLOCK.key(), getId(block))
        ).map(Holder::tags).orElseGet(Stream::empty);
    }
}

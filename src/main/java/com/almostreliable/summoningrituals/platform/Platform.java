package com.almostreliable.summoningrituals.platform;

import com.almostreliable.summoningrituals.Constants;
import com.almostreliable.summoningrituals.Registration;
import com.almostreliable.summoningrituals.network.ClientAltarUpdatePacket;
import com.almostreliable.summoningrituals.network.SacrificeParticlePacket;
import com.almostreliable.summoningrituals.recipe.component.BlockReference;
import com.almostreliable.summoningrituals.util.SerializeUtils;
import com.almostreliable.summoningrituals.util.Utils;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class Platform {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

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

    public static void sendProgressUpdate(Level level, BlockPos pos, int progress) {
        ClientAltarUpdatePacket.progressUpdate(level, pos, progress, Platform::sendPacket);
    }

    public static void sendProcessTimeUpdate(Level level, BlockPos pos, int processTime) {
        ClientAltarUpdatePacket.processTimeUpdate(level, pos, processTime, Platform::sendPacket);
    }

    public static void sendParticleEmit(Level level, List<BlockPos> positions) {
        sendPacket(level, positions.get(0), SacrificeParticlePacket.CHANNEL, SacrificeParticlePacket.encode(positions));
    }

    private static void sendPacket(Level level, BlockPos pos, ResourceLocation channel, FriendlyByteBuf packet) {
        var chunk = level.getChunkAt(pos);
        ((ServerChunkCache) chunk.level.chunkSource).chunkMap
            .getPlayers(chunk.pos, false)
            .forEach(serverPlayer -> ServerPlayNetworking.send(serverPlayer, channel, packet));
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

    // taken and adapted from Minecraft Forge
    public static ItemStack itemStackFromJson(JsonObject json) {
        String itemName = GsonHelper.getAsString(json, "item");
        Item item = getItem(itemName);
        if (json.has("nbt")) {
            CompoundTag nbt = getNBT(json.get("nbt"));
            CompoundTag tmp = new CompoundTag();
            tmp.put("tag", nbt);
            tmp.putString("id", itemName);
            tmp.putInt("Count", GsonHelper.getAsInt(json, "count", 1));
            return ItemStack.of(tmp);
        }
        return new ItemStack(item, GsonHelper.getAsInt(json, "count", 1));
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

    // taken and adapted from Minecraft Forge
    private static Item getItem(String itemName) {
        var itemKey = new ResourceLocation(itemName);
        if (!Registry.ITEM.containsKey(itemKey)) throw new JsonSyntaxException("Unknown item '" + itemName + "'");
        var item = Registry.ITEM.get(itemKey);
        //noinspection ObjectEquality
        if (item == Items.AIR) throw new JsonSyntaxException("Invalid item: " + itemName);
        return Objects.requireNonNull(item);
    }

    // taken and adapted from Minecraft Forge
    private static CompoundTag getNBT(JsonElement element) {
        try {
            if (element.isJsonObject()) return TagParser.parseTag(GSON.toJson(element));
            return TagParser.parseTag(GsonHelper.convertToString(element, "nbt"));
        } catch (CommandSyntaxException e) {
            throw new JsonSyntaxException("Invalid NBT Entry: " + e);
        }
    }
}

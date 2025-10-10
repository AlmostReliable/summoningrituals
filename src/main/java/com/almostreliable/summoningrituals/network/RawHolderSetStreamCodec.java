package com.almostreliable.summoningrituals.network;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.Optional;

public class RawHolderSetStreamCodec<T> implements StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>> {

    private final ResourceKey<? extends Registry<T>> registryKey;
    private final Codec<HolderSet<T>> originalCodec;
    private final StreamCodec<ByteBuf, Tag> nbtCodec = ByteBufCodecs.tagCodec(() -> NbtAccounter.create(2097152L));

    public RawHolderSetStreamCodec(ResourceKey<? extends Registry<T>> registryKey) {
        this.registryKey = registryKey;
        this.originalCodec = RegistryCodecs.homogeneousList(registryKey);
    }

    @Override
    public HolderSet<T> decode(RegistryFriendlyByteBuf buffer) {
        var tag = nbtCodec.decode(buffer);
        var registryAccess = buffer.registryAccess();
        var registry = registryAccess.registry(registryKey);

        if (registry.isPresent()) {
            var regOps = registryAccess.createSerializationContext(NbtOps.INSTANCE);
            return originalCodec.parse(regOps, tag).getOrThrow();
        }

        if (tag.getType().equals(StringTag.TYPE)) {
            var idOrTag = tag.getAsString();
            if (idOrTag.startsWith("#")) {
                var tagRaw = idOrTag.substring(1);
                var tagKey = TagKey.create(registryKey, ResourceLocation.parse(tagRaw));
                return new RawHolderSet<>(Optional.empty(), Optional.of(tagKey), Optional.empty());
            }

            var id = ResourceLocation.parse(idOrTag);
            return new RawHolderSet<>(Optional.of(List.of(id)), Optional.empty(), Optional.empty());
        }

        if (tag instanceof ListTag list) {
            var ids = list.stream()
                          .filter(t -> t.getType().equals(StringTag.TYPE))
                          .map(t -> ResourceLocation.parse(t.getAsString())) // TODO error handling? Encode should already ensure this I guess.
                          .toList();
            return new RawHolderSet<>(Optional.of(ids), Optional.empty(), Optional.empty());
        }

        return new RawHolderSet<>(Optional.empty(), Optional.empty(), Optional.of(tag));
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, HolderSet<T> value) {
        var registryAccess = buffer.registryAccess();
        var registry = registryAccess.registry(registryKey);
        if (registry.isEmpty()) {
            // Should only be called in server side and here the registry should always be present
            throw new IllegalStateException("Registry not found: " + registryKey);
        }

        var regOps = registryAccess.createSerializationContext(NbtOps.INSTANCE);
        // TODO error handling?
        var nbt = originalCodec.encodeStart(regOps, value).getOrThrow();
        nbtCodec.encode(buffer, nbt);
    }
}

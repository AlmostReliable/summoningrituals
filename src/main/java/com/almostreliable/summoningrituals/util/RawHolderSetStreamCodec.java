package com.almostreliable.summoningrituals.util;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RawHolderSetStreamCodec<T> implements StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>> {

    private static final long TWO_MEBIBYTES = 1 << 21;
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final Codec<HolderSet<T>> originalCodec;
    private final StreamCodec<ByteBuf, Tag> nbtCodec = ByteBufCodecs.tagCodec(() -> NbtAccounter.create(TWO_MEBIBYTES));

    public RawHolderSetStreamCodec(ResourceKey<? extends Registry<T>> registryKey) {
        this.registryKey = registryKey;
        this.originalCodec = RegistryCodecs.homogeneousList(registryKey);
    }

    @Override
    public HolderSet<T> decode(RegistryFriendlyByteBuf buffer) {
        var tag = nbtCodec.decode(buffer);
        var registryAccess = buffer.registryAccess();
        var registry = registryAccess.lookup(registryKey);

        if (registry.isPresent()) {
            var regOps = registryAccess.createSerializationContext(NbtOps.INSTANCE);
            return originalCodec.parse(regOps, tag).getOrThrow();
        }

        if (tag instanceof StringTag(var idOrTag)) {
            if (idOrTag.startsWith("#")) {
                var tagRaw = idOrTag.substring(1);
                var tagKey = TagKey.create(registryKey, Identifier.parse(tagRaw));
                return new RawHolderSet<>(Optional.empty(), Optional.of(tagKey), Optional.empty());
            }

            var id = Identifier.parse(idOrTag);
            return new RawHolderSet<>(Optional.of(List.of(id)), Optional.empty(), Optional.empty());
        }

        if (tag instanceof ListTag list) {
            var ids = new ArrayList<Identifier>();
            for (var t : list) {
                if (!(t instanceof StringTag(var id))) continue;
                ids.add(Identifier.parse(id));
            }
            return new RawHolderSet<>(Optional.of(ids), Optional.empty(), Optional.empty());
        }

        return new RawHolderSet<>(Optional.empty(), Optional.empty(), Optional.of(tag));
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, HolderSet<T> value) {
        var registryAccess = buffer.registryAccess();
        var registry = registryAccess.lookup(registryKey);
        if (registry.isEmpty()) {
            throw new IllegalStateException("registry not found: " + registryKey);
        }

        var regOps = registryAccess.createSerializationContext(NbtOps.INSTANCE);

        try {
            var nbt = originalCodec.encodeStart(regOps, value).getOrThrow();
            nbtCodec.encode(buffer, nbt);
        } catch (Exception e) {
            throw new IllegalArgumentException("failed to encode: " + value, e);
        }
    }
}

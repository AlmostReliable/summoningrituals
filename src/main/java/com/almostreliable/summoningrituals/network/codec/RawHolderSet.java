package com.almostreliable.summoningrituals.network.codec;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

import com.mojang.datafixers.util.Either;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("OptionalContainsCollection")
public record RawHolderSet<T>(
    Optional<List<ResourceLocation>> ids, Optional<TagKey<T>> tag, Optional<Tag> otherData
) implements HolderSet<T> {

    @Override
    public Stream<Holder<T>> stream() {
        return Stream.empty();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        //noinspection OptionalIsPresent
        if (tag.isPresent()) {
            return Either.left(tag.get());
        }

        return Either.right(Collections.emptyList());
    }

    @Override
    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        return Optional.empty();
    }

    @Override
    public Holder<T> get(int index) {
        throw new IndexOutOfBoundsException("RawHolderSet does not support get(int)");
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return false;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return false;
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return tag;
    }

    @Override
    public Iterator<Holder<T>> iterator() {
        return Collections.emptyIterator();
    }
}

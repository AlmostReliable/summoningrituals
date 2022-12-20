package com.almostreliable.summoningrituals.platform;

import net.minecraft.nbt.Tag;

public interface TagSerializable<T extends Tag> {
    T serialize();

    void deserialize(T tag);
}

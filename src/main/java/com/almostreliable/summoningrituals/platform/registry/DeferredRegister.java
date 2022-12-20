package com.almostreliable.summoningrituals.platform.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class DeferredRegister<T> {

    private final Registry<T> registry;
    private final String modId;
    private final List<RegistryObject<? extends T>> entries = new ArrayList<>();

    private DeferredRegister(Registry<T> registry, String modId) {
        this.registry = registry;
        this.modId = modId;
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry, String modId) {
        return new DeferredRegister<>(registry, modId);
    }

    public <R extends T> RegistryObject<R> register(String name, Supplier<R> supplier) {
        ResourceLocation id = new ResourceLocation(modId, name);
        var o = new RegistryObject<>(id, supplier.get());
        entries.add(o);
        return o;
    }

    public void register() {
        entries.forEach(ro -> Registry.register(registry, ro.id(), ro.get()));
    }
}

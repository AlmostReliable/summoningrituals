package com.almostreliable.summoningrituals;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

@SuppressWarnings("UnstableApiUsage")
public class SummoningRituals implements ModInitializer {
    @Override
    public void onInitialize() {
        Registration.init();
        ItemStorage.SIDED.registerForBlockEntity(
            (blockEntity, direction) -> blockEntity.exposeStorage(),
            Registration.ALTAR_ENTITY.get()
        );
    }
}

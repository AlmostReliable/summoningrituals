package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.platform.Platform;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@SuppressWarnings("WeakerAccess")
public class SummoningRitualsClient {
    public void onInitializeClient() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(Platform::registerBlockEntityRenderer);
    }
}

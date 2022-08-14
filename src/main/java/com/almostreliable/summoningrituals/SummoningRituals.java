package com.almostreliable.summoningrituals;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BuildConfig.MOD_ID)
public class SummoningRituals {
    public SummoningRituals() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Setup.init(modEventBus);
    }
}

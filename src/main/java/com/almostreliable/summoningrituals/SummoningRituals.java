package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@SuppressWarnings("WeakerAccess")
@Mod(BuildConfig.MOD_ID)
public class SummoningRituals {

    public SummoningRituals() {
        onInitialize();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> {
            var client = new SummoningRitualsClient();
            return client::onInitializeClient;
        });
    }

    public void onInitialize() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(SummoningRituals::onCommonSetup);
        Registration.init(modEventBus);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }
}

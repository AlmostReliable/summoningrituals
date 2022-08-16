package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.altar.AltarRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BuildConfig.MOD_ID)
public class SummoningRituals {

    public SummoningRituals() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(SummoningRituals::onClientSetup);
        Setup.init(modEventBus);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(Setup.ALTAR_ENTITY.get(), AltarRenderer::new);
        });
    }
}

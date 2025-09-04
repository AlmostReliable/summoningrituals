package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.network.PacketHandler;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@SuppressWarnings("WeakerAccess")
@Mod(ModConstants.MOD_ID)
public class SummoningRituals {

    public SummoningRituals(IEventBus eventBus) {
        Registration.init(eventBus);
        eventBus.addListener(SummoningRituals::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }
}

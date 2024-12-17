package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.network.PacketHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@SuppressWarnings("WeakerAccess")
@Mod(ModConstants.MOD_ID)
public class SummoningRituals {

    public SummoningRituals(IEventBus eventBus) {
        eventBus.addListener(SummoningRituals::onCommonSetup);
        eventBus.addListener(SummoningRituals::onRegistryEvent);
        eventBus.addListener(SummoningRituals::onCreativeTabContents);
        Registration.init(eventBus);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    private static void onRegistryEvent(RegisterEvent event) {
        Registration.Tab.registerTab(event);
    }

    private static void onCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        Registration.Tab.initContents(event);
    }
}

package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.core.ReloadListener;
import com.almostreliable.summoningrituals.data.DataGeneration;
import com.almostreliable.summoningrituals.network.PacketHandler;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@SuppressWarnings("WeakerAccess")
@Mod(ModConstants.MOD_ID)
public class SummoningRituals {

    public SummoningRituals(IEventBus eventBus, ModContainer modContainer) {
        Registration.init(eventBus);
        PacketHandler.init(eventBus);
        Config.init(modContainer);
        eventBus.addListener(SummoningRituals::registerReloadListener);
        eventBus.addListener(DataGeneration::init);
    }

    private static void registerReloadListener(AddReloadListenerEvent event) {
        event.addListener(ReloadListener.INSTANCE);
    }

    public static ResourceLocation getRL(String key) {
        return ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, key);
    }
}

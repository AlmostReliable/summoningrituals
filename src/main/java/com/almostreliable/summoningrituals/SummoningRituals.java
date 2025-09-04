package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.data.DataGeneration;
import com.almostreliable.summoningrituals.network.PacketHandler;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@SuppressWarnings("WeakerAccess")
@Mod(ModConstants.MOD_ID)
public class SummoningRituals {

    public SummoningRituals(IEventBus eventBus) {
        Registration.init(eventBus);
        PacketHandler.init();
        eventBus.addListener(DataGeneration::init);
    }
}

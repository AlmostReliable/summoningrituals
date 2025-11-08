package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.data.DataGeneration;
import com.almostreliable.summoningrituals.network.PacketHandler;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@SuppressWarnings("WeakerAccess")
@Mod(ModConstants.MOD_ID)
public final class SummoningRituals {

    public static final Logger LOGGER = LogUtils.getLogger();

    public SummoningRituals(IEventBus eventBus, ModContainer modContainer) {
        Registration.init(eventBus);
        PacketHandler.init(eventBus);
        Config.init(modContainer);
        eventBus.addListener(DataGeneration::init);
        NeoForge.EVENT_BUS.addListener(SummoningRituals::onEntityDeathLoot);
    }

    public static ResourceLocation getRL(String key) {
        return ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, key);
    }

    private static void onEntityDeathLoot(LivingDropsEvent event) {
        if (event.getEntity().getTags().contains(AltarBlockEntity.SACRIFICE_TAG)) {
            event.setCanceled(true);
        }
    }
}

package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.data.DataGeneration;
import com.almostreliable.summoningrituals.network.PacketHandler;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

    @SuppressWarnings("resource")
    private static void onEntityDeathLoot(LivingDropsEvent event) {
        var entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        if (entity.getTags().contains(AltarBlockEntity.SACRIFICE_TAG)) {
            var pos = entity.blockPosition();
            var rand = level.random;
            level.sendParticles(
                ParticleTypes.SOUL,
                pos.getX() + rand.nextDouble(),
                pos.getY() + 1,
                pos.getZ() + rand.nextDouble(),
                6,
                0,
                0,
                0,
                0.05
            );
            event.setCanceled(true);
        }
    }
}

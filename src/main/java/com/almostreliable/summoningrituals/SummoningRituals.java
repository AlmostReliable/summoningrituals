package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.altar.AltarBlockEntity;
import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.core.ModRegistration;
import com.almostreliable.summoningrituals.data.DataGeneration;
import com.almostreliable.summoningrituals.network.PacketHandler;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TriState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@SuppressWarnings("WeakerAccess")
@Mod(ModConstants.MOD_ID)
public final class SummoningRituals {

    public static final Logger LOGGER = LogUtils.getLogger();

    public SummoningRituals(IEventBus eventBus, ModContainer modContainer) {
        ModRegistration.init(eventBus);
        PacketHandler.init(eventBus);
        Config.init(modContainer);
        eventBus.addListener(DataGeneration::initClient);
        eventBus.addListener(DataGeneration::initServer);
        NeoForge.EVENT_BUS.addListener(SummoningRituals::onEntityDeathLoot);
        NeoForge.EVENT_BUS.addListener(SummoningRituals::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(SummoningRituals::onDatapackSync);
    }

    public static Identifier getRL(String key) {
        return Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, key);
    }

    @SuppressWarnings("resource")
    private static void onEntityDeathLoot(LivingDropsEvent event) {
        var entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        if (entity.entityTags().contains(AltarBlockEntity.SACRIFICE_TAG)) {
            var pos = entity.blockPosition();
            var rand = level.getRandom();
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

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(ModRegistration.ALTAR_RECIPE_TYPE.get());
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().is(ModRegistration.PATTERN_GENERATOR_ITEM)) {
            // prevent pattern generator from opening menus
            event.setUseBlock(TriState.FALSE);
        }
    }
}

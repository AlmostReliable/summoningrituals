package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.altar.AltarBlock;
import com.almostreliable.summoningrituals.altar.AltarRenderer;
import com.almostreliable.summoningrituals.network.PacketHandler;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.almostreliable.summoningrituals.util.TextUtils.f;

@Mod(BuildConfig.MOD_ID)
public class SummoningRituals {

    public SummoningRituals() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(SummoningRituals::onCommonSetup);
        modEventBus.addListener(SummoningRituals::onClientSetup);
        var forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.addListener(EventPriority.LOWEST, SummoningRituals::onRightClick);
        forgeEventBus.addListener(SummoningRituals::onLivingDrops);
        Setup.init(modEventBus);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockEntityRenderers.register(Setup.ALTAR_ENTITY.get(), AltarRenderer::new));
    }

    private static void onRightClick(RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        var player = event.getEntity();
        var item = player.getItemInHand(InteractionHand.MAIN_HAND);
        var block = event.getLevel().getBlockState(event.getPos()).getBlock();

        if (player.isShiftKeyDown() == item.isEmpty() && block instanceof AltarBlock) {
            event.setUseBlock(Result.ALLOW);
            event.setUseItem(Result.DENY);
            event.setCanceled(false);
        }
    }

    private static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().getTags().contains(f("{}_sacrificed", BuildConfig.MOD_ID))) {
            event.setCanceled(true);
        }
    }
}

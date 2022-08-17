package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.altar.AltarRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BuildConfig.MOD_ID)
public class SummoningRituals {

    public SummoningRituals() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(SummoningRituals::onClientSetup);
        var forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.addListener(SummoningRituals::onRightClick);
        Setup.init(modEventBus);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(Setup.ALTAR_BLOCK.get(), RenderType.cutout());
            BlockEntityRenderers.register(Setup.ALTAR_ENTITY.get(), AltarRenderer::new);
        });
    }

    private static void onRightClick(RightClickBlock event) {
        if (!event.getPlayer().isShiftKeyDown() &&
            event.getWorld().getBlockState(event.getPos()).getBlock().equals(Setup.ALTAR_BLOCK.get())) {
            event.setUseBlock(Result.ALLOW);
            event.setUseItem(Result.DENY);
            event.setCanceled(false);
        }
    }
}

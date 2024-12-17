package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.altar.AltarRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = ModConstants.MOD_ID, dist = Dist.CLIENT)
public class SummoningRitualsClient {

    public SummoningRitualsClient(IEventBus eventBus) {
        eventBus.addListener(this::registerRenderers);
    }

    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.ALTAR_ENTITY.get(), AltarRenderer::new);
    }
}

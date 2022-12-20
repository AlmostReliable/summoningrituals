package com.almostreliable.summoningrituals;

import com.almostreliable.summoningrituals.altar.AltarRenderer;
import com.almostreliable.summoningrituals.network.PacketHandler;
import com.almostreliable.summoningrituals.platform.Platform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public class SummoningRitualsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PacketHandler.initS2C();
        Platform.registerBlockEntityRenderer(Registration.ALTAR_ENTITY.get(), AltarRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(Registration.ALTAR_BLOCK.get(), RenderType.cutout());
    }
}

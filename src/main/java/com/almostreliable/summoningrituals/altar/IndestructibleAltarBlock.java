package com.almostreliable.summoningrituals.altar;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public class IndestructibleAltarBlock extends AltarBlock{
    public IndestructibleAltarBlock() {
        super(Properties.of(Material.STONE).strength(-1.0F, 3600000.0F).sound(SoundType.STONE));
    }
}

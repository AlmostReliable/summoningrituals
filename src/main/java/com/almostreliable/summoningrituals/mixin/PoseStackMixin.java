package com.almostreliable.summoningrituals.mixin;

import com.almostreliable.summoningrituals.extension.PoseStackExtension;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PoseStack.class)
public abstract class PoseStackMixin implements PoseStackExtension {

    @Shadow
    public abstract void scale(float x, float y, float z);

    @Override
    public void summoning$scale(float scale) {
        scale(scale, scale, scale);
    }
}

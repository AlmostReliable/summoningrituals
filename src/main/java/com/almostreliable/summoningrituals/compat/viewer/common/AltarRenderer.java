package com.almostreliable.summoningrituals.compat.viewer.common;

import com.almostreliable.summoningrituals.Registration;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class AltarRenderer extends SizedItemRenderer {

    private final ItemStack altar;

    public AltarRenderer(int size) {
        super(size);
        altar = new ItemStack(Registration.ALTAR_ITEM.get());
    }

    @Override
    public void render(PoseStack stack, @Nullable ItemStack item) {
        super.render(stack, altar);
    }
}

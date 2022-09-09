package com.almostreliable.summoningrituals.compat.jei.ingredient.item;

import com.almostreliable.summoningrituals.Setup;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;

public class AltarRenderer extends SizedItemRenderer {

    private final ItemStack altar;

    public AltarRenderer(int size) {
        super(size);
        altar = new ItemStack(Setup.ALTAR_ITEM.get());
    }

    public void render(PoseStack stack) {
        render(stack, altar);
    }
}

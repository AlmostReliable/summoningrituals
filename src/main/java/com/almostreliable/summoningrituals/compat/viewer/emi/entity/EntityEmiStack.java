package com.almostreliable.summoningrituals.compat.viewer.emi.entity;

import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredient;
import com.almostreliable.summoningrituals.compat.viewer.common.EntityIngredientRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiStack;

import java.util.ArrayList;
import java.util.List;

public final class EntityEmiStack extends EmiStack {

    private final EntityIngredient entityIngredient;
    private final boolean scissor;

    @SuppressWarnings("AssignmentToSuperclassField")
    private EntityEmiStack(EntityIngredient entityIngredient, boolean scissor) {
        this.entityIngredient = entityIngredient;
        this.scissor = scissor;
        this.amount = entityIngredient.getEntityInfo().count();
    }

    public static EntityEmiStack input(EntityIngredient entityIngredient) {
        return new EntityEmiStack(entityIngredient, false);
    }

    public static EntityEmiStack output(EntityIngredient entityIngredient) {
        return new EntityEmiStack(entityIngredient, true);
    }

    @Override
    public EmiStack copy() {
        return new EntityEmiStack(entityIngredient.copy(), scissor);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float delta, int flags) {
        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        {
            poseStack.translate(x, y, 0);
            EntityIngredientRenderer.render(guiGraphics, entityIngredient, flags == -1 && scissor, (flags & RENDER_AMOUNT) != 0);
        }
        poseStack.popPose();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public DataComponentPatch getComponentChanges() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    public Object getKey() {
        return entityIngredient.getEntityInfo().entity().value();
    }

    @Override
    public ResourceLocation getId() {
        return entityIngredient.getId();
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of(); // entity stacks are not added to the index, this is only for search
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        var mc = Minecraft.getInstance();
        var tooltips = entityIngredient.getTooltip(true, mc.options.advancedItemTooltips);
        var res = new ArrayList<ClientTooltipComponent>();
        for (var tooltip : tooltips) {
            res.add(EmiTooltipComponents.of(tooltip));
        }
        EmiTooltipComponents.appendModName(res, getId().getNamespace());
        return res;
    }

    @Override
    public Component getName() {
        return entityIngredient.getDisplayName();
    }
}

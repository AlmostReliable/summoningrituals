package com.almostreliable.summoningrituals.compat.kubejs.event;

import com.almostreliable.summoningrituals.ModConstants;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface KubeEvents {

    EventGroup GROUP = EventGroup.of(ModConstants.MOD_NAME.replace(" ", ""));

    // client
    EventHandler RITUAL_RENDERER_REGISTRY = GROUP.client("ritualRendererRegistration", () -> RitualRendererRegistryKubeEvent.class);
    EventHandler MODIFY_CONDITIONS_TOOLTIP = GROUP.client("modifyConditionsTooltip", () -> ModifyConditionsTooltipEvent.class);

    // server
    EventHandler SUMMONING_START = GROUP.server("start", () -> SummoningKubeEvent.class).hasResult();
    EventHandler SUMMONING_COMPLETE = GROUP.server("complete", () -> SummoningKubeEvent.class);
}

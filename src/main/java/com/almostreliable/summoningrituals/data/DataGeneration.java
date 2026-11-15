package com.almostreliable.summoningrituals.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class DataGeneration {

    private DataGeneration() {}

    public static void initClient(GatherDataEvent.Client event) {
        event.createProvider(SummoningLang::new);
        event.createProvider(SummoningModels::new);
    }

    public static void initServer(GatherDataEvent.Server event) {
        event.createProvider(SummoningLoot::new);
        event.createProvider(SummoningTags::new);
    }
}

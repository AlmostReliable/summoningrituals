package com.almostreliable.summoningrituals.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class DataGeneration {

    private DataGeneration() {}

    public static void init(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();
        var registryAccess = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new SummoningLang(output));
        generator.addProvider(event.includeClient(), new SummoningModels(output, existingFileHelper));

        generator.addProvider(event.includeServer(), new SummoningLoot(output, registryAccess));
        generator.addProvider(event.includeServer(), new SummoningTags(output, registryAccess, existingFileHelper));
    }
}

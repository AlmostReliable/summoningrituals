package com.almostreliable.summoningrituals.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class DataGeneration {

    private DataGeneration() {}

    public static void init(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        var registryAccess = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new SummoningLang(output));

        generator.addProvider(event.includeServer(), new SummoningLoot(output, registryAccess));
        generator.addProvider(event.includeServer(), new SummoningTags(output, registryAccess, existingFileHelper));
    }
}

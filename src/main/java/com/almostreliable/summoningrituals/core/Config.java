package com.almostreliable.summoningrituals.core;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {

    private static final ModConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;

    static {
        var commonPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();
    }

    private Config() {}

    public static void init(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    public static final class CommonConfig {

        public final ModConfigSpec.IntValue altarInventorySize;

        private CommonConfig(ModConfigSpec.Builder builder) {
            altarInventorySize = builder.comment("The number of slots in the Altar's inventory.")
                .comment("To apply changes to existing Altars, you will need to break and replace them.")
                .defineInRange("requests", 16, 1, 64);
        }
    }
}
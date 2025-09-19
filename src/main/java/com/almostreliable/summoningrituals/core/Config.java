package com.almostreliable.summoningrituals.core;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {

    private static final ModConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;
    private static final ModConfigSpec CLIENT_SPEC;
    public static final ClientConfig CLIENT;

    static {
        var commonPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();

        var clientPair = new ModConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = clientPair.getRight();
        CLIENT = clientPair.getLeft();
    }

    private Config() {}

    public static void init(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    public static final class CommonConfig {

        public final ModConfigSpec.IntValue altarInventorySize;

        private CommonConfig(ModConfigSpec.Builder builder) {
            altarInventorySize = builder.comment("The number of slots in the Altar's inventory.")
                .comment("To apply changes to existing Altars, you will need to break and replace them.")
                .defineInRange("requests", 16, 1, 64);
        }
    }

    public static final class ClientConfig {

        public final ModConfigSpec.IntValue altarRenderDistance;

        private ClientConfig(ModConfigSpec.Builder builder) {
            altarRenderDistance = builder.comment("The maximum distance at which an Altar will render its inventory contents.")
                .defineInRange("altar_render_distance", 32, 1, 128);
        }
    }
}
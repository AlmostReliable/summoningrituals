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

        public final ModConfigSpec.IntValue inventorySize;
        public final ModConfigSpec.BooleanValue announceCommand;

        private CommonConfig(ModConfigSpec.Builder builder) {
            inventorySize = builder.comment("The number of slots in the Altar's inventory.")
                .comment("To apply changes to existing Altars, you will need to break and replace them.")
                .defineInRange("inventory_size", 16, 1, 64);
            announceCommand = builder.comment("Whether to announce in the log when a recipe output command is invoked.")
                .comment("This is useful to avoid confusion where the command originates from.")
                .define("announce_command", true);
        }
    }

    public static final class ClientConfig {

        public final ModConfigSpec.IntValue renderDistance;
        public final ModConfigSpec.BooleanValue candleParticles;
        public final ModConfigSpec.IntValue patternPreviewSearchRadius;
        public final ModConfigSpec.IntValue patternPreviewTicks;

        private ClientConfig(ModConfigSpec.Builder builder) {
            renderDistance = builder.comment("The maximum distance at which an Altar will render its inventory contents.")
                .defineInRange("render_distance", 32, 1, 128);
            candleParticles = builder.comment("Whether to render the fire particles on candles on Altars.")
                .define("candle_particles", true);
            patternPreviewSearchRadius = builder.comment("The maximum distance to search for an Altar when previewing the block pattern.")
                .defineInRange("pattern_preview_search_radius", 16, 1, 64);
            patternPreviewTicks = builder.comment("The number of ticks to display the block pattern preview for.")
                .defineInRange("pattern_preview_ticks", 400, 10, 1200);
        }
    }
}

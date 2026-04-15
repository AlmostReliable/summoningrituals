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

        public final ModConfigSpec.IntValue inventoryRenderDistance;
        public final ModConfigSpec.BooleanValue candleParticles;
        public final ModConfigSpec.IntValue altarSearchRadius;
        public final ModConfigSpec.IntValue previewTicksPerBlock;
        public final ModConfigSpec.IntValue previewTicksMin;
        public final ModConfigSpec.IntValue previewTicksMax;
        public final ModConfigSpec.BooleanValue previewBlockStateAware;

        private ClientConfig(ModConfigSpec.Builder builder) {
            builder.push("general");
            inventoryRenderDistance = builder.comment("The maximum distance at which an Altar will render its inventory contents.")
                .defineInRange("inventory_render_distance", 32, 1, 128);
            candleParticles = builder.comment("Whether to render the fire particles on candles on Altars.")
                .define("candle_particles", true);
            builder.pop();

            builder.push("preview");
            altarSearchRadius = builder.comment("The maximum distance to search for an Altar when previewing the block pattern.")
                .defineInRange("altar_search_radius", 16, 1, 64);
            previewTicksPerBlock = builder.comment("The number of ticks per block to display the block pattern preview for.")
                .defineInRange("preview_ticks_per_block", 60, 1, 100);
            previewTicksMin = builder.comment("The minimum number of ticks to preview the block pattern for.")
                .defineInRange("preview_ticks_min", 200, 10, 1200);
            previewTicksMax = builder.comment("The maximum number of ticks to preview the block pattern for.")
                .defineInRange("preview_ticks_max", 1200, 10, 6000);
            previewBlockStateAware = builder.comment(
                    "Whether the preview should check for exact matches of block states rather than for the correct block only.",
                    "The recipe starting logic will always check for the whole block state."
                )
                .define("preview_block_state_aware", false);
            builder.pop();
        }
    }
}

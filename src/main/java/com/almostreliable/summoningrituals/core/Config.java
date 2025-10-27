package com.almostreliable.summoningrituals.core;

import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.SummoningRituals;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.function.Supplier;

public final class Config {

    public static CommonConfig COMMON;
    public static ClientConfig CLIENT;

    private Config() {}

    public static void init() {
        COMMON = new CommonConfig(ConfigLoader.loadOrCreate(
            "common",
            CommonConfigData.class,
            CommonConfigData::new
        ));
        CLIENT = new ClientConfig(ConfigLoader.loadOrCreate(
            "client",
            ClientConfigData.class,
            ClientConfigData::new
        ));
    }

    public static void reload() {
        SummoningRituals.LOGGER.info("Reloading configs");
        init();
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static final class CommonConfig {

        public final int altarInventorySize;

        private CommonConfig(CommonConfigData data) {
            this.altarInventorySize = Math.clamp(1, data.altarInventorySize, 64);
        }
    }

    public static final class ClientConfig {

        public final int altarRenderDistance;
        public final Map<String, Float> entitySizes;
        public final Map<String, Float> entityOffsets;

        private ClientConfig(ClientConfigData data) {
            this.altarRenderDistance = Math.clamp(1, data.altarRenderDistance, 128);
            this.entitySizes = Map.copyOf(data.entitySizes);
            this.entityOffsets = Map.copyOf(data.entityOffsets);
        }
    }

    @SuppressWarnings("FieldMayBeStatic")
    private static final class CommonConfigData {

        private final int altarInventorySize = 16;
    }

    @SuppressWarnings("FieldMayBeStatic")
    private static final class ClientConfigData {

        private final int altarRenderDistance = 32;
        private final Map<String, Float> entitySizes = Map.of(
            ResourceLocation.withDefaultNamespace("phantom").toString(), 8f,
            ResourceLocation.withDefaultNamespace("ghast").toString(), 1.9f
        );
        private final Map<String, Float> entityOffsets = Map.of(
            ResourceLocation.withDefaultNamespace("phantom").toString(), -6f,
            ResourceLocation.withDefaultNamespace("ghast").toString(), -6f,
            ResourceLocation.withDefaultNamespace("blaze").toString(), 2f
        );
    }

    private static final class ConfigLoader {

        private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

        private static <T> T loadOrCreate(String fileName, Class<T> configType, Supplier<T> defaultConfigSupplier) {
            var filePath = getConfigDir().resolve(fileName + ".json");

            if (Files.exists(filePath)) {
                try (var reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                    var parsedJson = JsonParser.parseReader(reader);
                    if (!(parsedJson instanceof JsonObject jsonObject)) {
                        throw new IllegalStateException("Root of config must be a JSON object");
                    }

                    var defaultConfig = defaultConfigSupplier.get();
                    var defaultsJson = GSON.toJsonTree(defaultConfig).getAsJsonObject();

                    var changes = new StringBuilder();
                    var merged = mergeWithDefaults(jsonObject, defaultsJson, changes, "");

                    if (changes.isEmpty()) {
                        SummoningRituals.LOGGER.info("Loaded config file: {}", filePath.getFileName());
                    } else {
                        SummoningRituals.LOGGER.warn(
                            "Config file {} is missing or has mismatched entries; applying defaults for: {}",
                            filePath.getFileName(), changes
                        );
                        backup(filePath);
                        save(filePath, merged);
                    }

                    return GSON.fromJson(merged, configType);
                } catch (Exception e) {
                    SummoningRituals.LOGGER.error("Failed to read config file: {}", filePath.getFileName(), e);
                    SummoningRituals.LOGGER.warn("Backing up and recreating default config.");
                    backup(filePath);
                }
            } else {
                SummoningRituals.LOGGER.warn("Config file {} does not exist. Creating new one.", filePath.getFileName());
            }

            var value = defaultConfigSupplier.get();
            save(filePath, value);
            return value;
        }

        private static void backup(Path filePath) {
            var backupFile = filePath.resolveSibling(filePath.getFileName() + ".bak");
            try {
                Files.copy(filePath, backupFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                // ignore
            }
        }

        private static void save(Path filePath, Object data) {
            try {
                Files.createDirectories(filePath.getParent());
                try (
                    var writer = Files.newBufferedWriter(
                        filePath,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                    )
                ) {
                    GSON.toJson(data, writer);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to write config file: " + filePath.getFileName(), e);
            }
        }

        private static JsonObject mergeWithDefaults(JsonObject existing, JsonObject defaults, StringBuilder changes, String path) {
            var result = new JsonObject();
            for (var entry : existing.entrySet()) {
                result.add(entry.getKey(), entry.getValue());
            }

            for (var defaultEntry : defaults.entrySet()) {
                var defaultKey = defaultEntry.getKey();
                var defaultValue = defaultEntry.getValue();
                var fullPath = path.isEmpty() ? defaultKey : path + "." + defaultKey;
                if (!existing.has(defaultKey) || existing.get(defaultKey) == null || existing.get(defaultKey).isJsonNull()) {
                    result.add(defaultKey, defaultValue);
                    if (!changes.isEmpty()) changes.append(", ");
                    changes.append(fullPath);
                    continue;
                }

                var existingValue = existing.get(defaultKey);

                if (defaultValue instanceof JsonObject defaultValueObject && existingValue instanceof JsonObject existingValueObject) {
                    var mergedChild = mergeWithDefaults(existingValueObject, defaultValueObject, changes, fullPath);
                    result.add(defaultKey, mergedChild);
                } else if (defaultValue.isJsonArray() != existingValue.isJsonArray()
                    || defaultValue.isJsonObject() != existingValue.isJsonObject()
                    || (defaultValue.isJsonPrimitive() && existingValue.isJsonPrimitive() &&
                    !samePrimitiveKind(defaultValue, existingValue))) {
                    result.add(defaultKey, defaultValue);
                    if (!changes.isEmpty()) changes.append(", ");
                    changes.append(fullPath);
                } else {
                    result.add(defaultKey, existingValue);
                }
            }

            return result;
        }

        private static boolean samePrimitiveKind(JsonElement a, JsonElement b) {
            var ap = a.getAsJsonPrimitive();
            var bp = b.getAsJsonPrimitive();
            return (ap.isBoolean() && bp.isBoolean()) || (ap.isNumber() && bp.isNumber()) || (ap.isString() && bp.isString());
        }

        private static Path getConfigDir() {
            var base = FMLPaths.CONFIGDIR.get();
            var dir = base.resolve(ModConstants.MOD_ID);
            try {
                Files.createDirectories(dir);
                return dir;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create config directory: " + dir, e);
            }
        }
    }
}
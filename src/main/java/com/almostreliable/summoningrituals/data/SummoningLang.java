package com.almostreliable.summoningrituals.data;

import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.core.Constants;
import com.almostreliable.summoningrituals.recipe.condition.TimeCondition;
import com.almostreliable.summoningrituals.recipe.condition.custom.MoonPhaseCheck;

import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.data.LanguageProvider;

import org.apache.commons.lang3.text.WordUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public final class SummoningLang extends LanguageProvider {

    // @formatter:off

    // messages
    public static final LangEntry IN_PROGRESS = LangEntry.message("in_progress", "The ritual is already in progress.");
    public static final LangEntry MISSING_INPUTS = LangEntry.message("missing_inputs", "Some of the required item inputs couldn't be found in the Altar.");
    public static final LangEntry INVALID_INITIATOR = LangEntry.message("invalid_initiator", "No recipes have been found for this initiator.");
    public static final LangEntry MISSING_SACRIFICES = LangEntry.message("missing_sacrifices", "Not all sacrifices were found for the ritual.");
    public static final LangEntry FAILED_CONDITIONS = LangEntry.message("failed_conditions", "Not all conditions were met for the ritual.");
    public static final LangEntry MULTI_MATCH = LangEntry.message("multi_match", "More than one recipe found for this initiator.");

    // labels
    public static final LangEntry INITIATOR = LangEntry.label(Constants.INITIATOR, "Initiator");
    public static final LangEntry CONDITIONS = LangEntry.label(Constants.CONDITIONS, "Conditions");
    public static final LangEntry COMMANDS = LangEntry.label(Constants.COMMANDS, "Commands");

    // hints
    public static final LangEntry INSERT_LAST = LangEntry.hint("insert_last", "Insert last to start the ritual.");

    // conditions
    public static final LangEntry ALTAR_PROPERTIES = LangEntry.condition("altar_properties", "Altar Properties");
    public static final LangEntry BIOMES = LangEntry.condition("biomes", "Biomes");
    public static final LangEntry CLEAR = LangEntry.condition("weather_clear", "Clear");
    public static final LangEntry DIMENSION = LangEntry.condition("dimension", "Dimension");
    public static final LangEntry HEIGHT = LangEntry.condition("height", "Height");
    public static final LangEntry LIGHT_LEVEL = LangEntry.condition("light_level", "Light Level");
    public static final LangEntry MAXIMUM = LangEntry.condition("maximum", "Maximum");
    public static final LangEntry MINIMUM = LangEntry.condition("minimum", "Minimum");
    public static final LangEntry MOON_PHASE = LangEntry.condition("moon_phase", "Moon Phase");
    public static final LangEntry NO = LangEntry.condition("no", "No");
    public static final LangEntry NOT_THUNDERING = LangEntry.condition("weather_not_thundering", "Not Thundering");
    public static final LangEntry OPEN_SKY = LangEntry.condition("open_sky", "Open Sky");
    public static final LangEntry RAINING = LangEntry.condition("weather_raining", "Raining");
    public static final LangEntry SMOKED = LangEntry.condition("smoked", "Smoked");
    public static final LangEntry STRUCTURES = LangEntry.condition("structures", "Structures");
    public static final LangEntry THUNDERING = LangEntry.condition("weather_thundering", "Thundering");
    public static final LangEntry TIME = LangEntry.condition("time", "Time");
    public static final LangEntry WEATHER = LangEntry.condition("weather", "Weather");
    public static final LangEntry YES = LangEntry.condition("yes", "Yes");

    // enums
    public static final Map<MoonPhaseCheck.MoonPhase, LangEntry> MOON_PHASES = LangEntry.enumValues("condition", "moon_phases", MoonPhaseCheck.MoonPhase.values());
    public static final Map<TimeCondition.TimeType, LangEntry> TIME_TYPES = LangEntry.enumValues("condition", "time", TimeCondition.TimeType.values());

    // @formatter:on

    SummoningLang(PackOutput output) {
        super(output, ModConstants.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        for (var entry : LangEntry.ENTRIES) {
            add(entry.key, entry.value);
        }
    }

    public record LangEntry(String key, String value) implements Supplier<MutableComponent> {

        private static final Set<LangEntry> ENTRIES = new HashSet<>();

        public static LangEntry of(String prefix, String id, String value) {
            var entry = new LangEntry(String.format("%s.%s.%s", prefix, ModConstants.MOD_ID, id), value);
            ENTRIES.add(entry);
            return entry;
        }

        private static LangEntry message(String id, String value) {
            return of("message", id, value);
        }

        private static LangEntry label(String id, String value) {
            return of("label", id, value);
        }

        private static LangEntry hint(String id, String value) {
            return of("hint", id, value);
        }

        private static LangEntry condition(String id, String value) {
            return of("condition", id, value);
        }

        @SafeVarargs
        private static <T extends Enum<?>> Map<T, LangEntry> enumValues(String prefix, String idPrefix, T... enumValues) {
            var enumEntries = new HashMap<T, LangEntry>();

            for (var enumValue : enumValues) {
                var id = enumValue.name().toLowerCase(Locale.ROOT);
                //noinspection deprecation
                var value = WordUtils.capitalizeFully(id.replace("_", " "));
                enumEntries.put(enumValue, of(prefix, idPrefix + "_" + id, value));
            }

            return enumEntries;
        }

        @Override
        public MutableComponent get() {
            return Component.translatable(key);
        }
    }
}

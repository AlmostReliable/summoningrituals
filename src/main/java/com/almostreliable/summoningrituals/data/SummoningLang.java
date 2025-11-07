package com.almostreliable.summoningrituals.data;

import com.almostreliable.summoningrituals.ModConstants;
import com.almostreliable.summoningrituals.core.Constants;

import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.data.LanguageProvider;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public final class SummoningLang extends LanguageProvider {

    // @formatter:off

    // blocks
    public static final LangEntry ALTAR = LangEntry.block(Constants.ALTAR, "Summoning Altar");
    public static final LangEntry INDESTRUCTIBLE_ALTAR = LangEntry.block(Constants.INDESTRUCTIBLE_ALTAR, "Indestructible Summoning Altar");
    
    // messages
    public static final LangEntry IN_PROGRESS = LangEntry.message("in_progress", "The ritual is already in progress.");
    public static final LangEntry MISSING_INPUTS = LangEntry.message("missing_inputs", "Some of the required item inputs couldn't be found in the Altar.");
    public static final LangEntry INVALID_CATALYST = LangEntry.message("invalid_catalyst", "No recipes have been found for this catalyst.");
    public static final LangEntry MISSING_SACRIFICES = LangEntry.message("missing_sacrifices", "Not all sacrifices were found for the ritual.");
    public static final LangEntry FAILED_CONDITIONS = LangEntry.message("failed_conditions", "Not all conditions were met for the ritual.");
    public static final LangEntry MULTI_MATCH = LangEntry.message("multi_match", "More than one recipe found for this catalyst.");
    
    // labels
    public static final LangEntry CATALYST = LangEntry.label(Constants.CATALYST, "Catalyst");
    public static final LangEntry CONDITIONS = LangEntry.label(Constants.CONDITIONS, "Conditions");
    
    // hints
    public static final LangEntry INSERT_LAST = LangEntry.hint("insert_last", "Insert last to start the ritual.");

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

        private static LangEntry block(String id, String value) {
            return of("block", id, value);
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

        public static LangEntry condition(String id, String value) {
            return of("condition", id, value);
        }

        @SafeVarargs
        public static <T extends Enum<?>> Map<T, LangEntry> enumValues(String prefix, String idPrefix, T... enumValues) {
            var enumEntries = new HashMap<T, LangEntry>();

            for (var enumValue : enumValues) {
                var id = enumValue.name().toLowerCase(Locale.ROOT);
                var value = StringUtils.capitalize(id);
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

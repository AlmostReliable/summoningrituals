package com.almostreliable.summoningrituals.data;

import com.almostreliable.summoningrituals.ModConstants;

import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public final class SummoningLang extends LanguageProvider {

    // @formatter:off

    // messages
    public static final LangEntry IN_PROGRESS = LangEntry.label("in_progress", "The ritual is already in progress.");
    public static final LangEntry MISSING_INPUTS = LangEntry.label("missing_inputs", "Some of the required item inputs couldn't be found.");

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

        private static LangEntry label(String id, String value) {
            return of("message", id, value);
        }

        @Override
        public MutableComponent get() {
            return Component.translatable(key, value);
        }
    }
}

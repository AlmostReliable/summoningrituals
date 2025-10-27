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

    // blocks
    public static final LangEntry ALTAR = LangEntry.block("altar", "Summoning Altar");
    public static final LangEntry INDESTRUCTIBLE_ALTAR = LangEntry.block("indestructible_altar", "Indestructible Summoning Altar");
    
    // messages
    public static final LangEntry IN_PROGRESS = LangEntry.message("in_progress", "The ritual is already in progress.");
    public static final LangEntry MISSING_INPUTS = LangEntry.message("missing_inputs", "Some of the required item inputs couldn't be found.");
    public static final LangEntry CONDITION_FAIL = LangEntry.message("condition_fail", "Not all conditions were met for the ritual.");
    
    // labels
    public static final LangEntry CATALYST = LangEntry.label("catalyst", "Catalyst");
    
    // hints
    public static final LangEntry INSERT_LAST = LangEntry.hint("insert_last", "Insert as the last item to start the ritual.");
    public static final LangEntry INSERT_RIGHT_CLICK = LangEntry.hint("insert_right_click", "Right click to insert an input item.");
    public static final LangEntry POP_SHIFT_RIGHT_CLICK = LangEntry.hint("pop_shift_right_click", "Shift right click with empty hand to remove the last inserted item.");

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

        @Override
        public MutableComponent get() {
            return Component.translatable(key);
        }
    }
}

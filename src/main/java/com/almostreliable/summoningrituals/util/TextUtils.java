package com.almostreliable.summoningrituals.util;

import com.almostreliable.summoningrituals.SummoningRitualsConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.regex.Pattern;

public final class TextUtils {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{}");

    private TextUtils() {}

    public static String f(String input, Object... args) {
        for (var arg : args) {
            input = PLACEHOLDER.matcher(input).replaceFirst(arg.toString());
        }
        for (var i = 0; i < args.length; i++) {
            input = input.replace("{" + i + "}", args[i].toString());
        }
        return input;
    }

    public static String translateAsString(String type, String key) {
        return translate(type, key).getString();
    }

    public static MutableComponent translate(String type, String key, ChatFormatting... color) {
        var output = Component.translatable(getTranslationKey(type, key));
        return color.length == 0 ? output : output.withStyle(color[0]);
    }

    public static MutableComponent colorize(String input, ChatFormatting color) {
        return Component.literal(input).withStyle(color);
    }

    private static String getTranslationKey(String type, String key) {
        return String.format("%s.%s.%s", type.toLowerCase(), SummoningRitualsConstants.MOD_ID, key);
    }
}

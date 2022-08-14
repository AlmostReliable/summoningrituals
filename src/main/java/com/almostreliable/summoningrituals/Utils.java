package com.almostreliable.summoningrituals;

import java.util.regex.Pattern;

public final class Utils {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{}");

    private Utils() {}

    public static String f(String input, Object... args) {
        for (var arg : args) {
            input = PLACEHOLDER.matcher(input).replaceFirst(arg.toString());
        }
        for (var i = 0; i < args.length; i++) {
            input = input.replace("{" + i + "}", args[i].toString());
        }
        return input;
    }
}

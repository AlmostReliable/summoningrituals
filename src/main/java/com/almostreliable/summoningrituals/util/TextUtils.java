package com.almostreliable.summoningrituals.util;

import com.almostreliable.summoningrituals.BuildConfig;
import com.almostreliable.summoningrituals.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
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

    public static ResourceLocation getRL(String key) {
        return new ResourceLocation(BuildConfig.MOD_ID, key);
    }

    public static String translateAsString(String type, String key) {
        return translate(type, key).getString();
    }

    public static MutableComponent translate(String type, String key, ChatFormatting... color) {
        var output = Component.translatable(getTranslationKey(type, key));
        return color.length == 0 ? output : output.withStyle(color[0]);
    }

    public static void sendPlayerMessage(
        @Nullable Player player, String translationKey, ChatFormatting color, Object... args
    ) {
        if (player == null) return;
        player.sendSystemMessage(
            Component.translatable(getTranslationKey(Constants.MESSAGE, translationKey), args).withStyle(color)
        );
    }

    public static MutableComponent colorize(String input, ChatFormatting color) {
        return Component.literal(input).withStyle(color);
    }

    private static String getTranslationKey(String type, String key) {
        return String.format("%s.%s.%s", type.toLowerCase(), BuildConfig.MOD_ID, key);
    }
}

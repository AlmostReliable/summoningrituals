package com.almostreliable.summoningrituals.util;

import com.almostreliable.summoningrituals.BuildConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.regex.Pattern;

public final class TextUtils {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{}");
    private static final UUID MOD_UUID = UUID.nameUUIDFromBytes(BuildConfig.MOD_ID.getBytes());

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

    public static TranslatableComponent translate(String type, String key, ChatFormatting... color) {
        var output = new TranslatableComponent(getTranslationKey(type, key));
        return color.length == 0 ? output : (TranslatableComponent) output.withStyle(color[0]);
    }

    public static TranslatableComponent translateWithArgs(
        String type, String key, ChatFormatting color, Object... args
    ) {
        var output = new TranslatableComponent(getTranslationKey(type, key), args);
        return (TranslatableComponent) output.withStyle(color);
    }

    public static void sendPlayerMessage(Player player, String translationKey, ChatFormatting color, Object... args) {
        player.sendMessage(
            translateWithArgs("message", translationKey, color, args),
            MOD_UUID
        );
    }

    public static TextComponent colorize(String input, ChatFormatting color) {
        return (TextComponent) new TextComponent(input).withStyle(color);
    }

    private static String getTranslationKey(String type, String key) {
        return String.format("%s.%s.%s", type.toLowerCase(), BuildConfig.MOD_ID, key);
    }
}

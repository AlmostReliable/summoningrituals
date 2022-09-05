package com.almostreliable.summoningrituals.util;

import com.almostreliable.summoningrituals.BuildConfig;
import com.almostreliable.summoningrituals.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
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

    public static String translateAsString(String type, String key) {
        return translate(type, key).getString();
    }

    public static TranslatableComponent translate(String type, String key, ChatFormatting... color) {
        var output = new TranslatableComponent(getTranslationKey(type, key));
        return color.length == 0 ? output : (TranslatableComponent) output.withStyle(color[0]);
    }

    public static void sendPlayerMessage(
        @Nullable Player player, String translationKey, ChatFormatting color, Object... args
    ) {
        if (player == null) return;
        player.sendMessage(
            new TranslatableComponent(getTranslationKey(Constants.MESSAGE, translationKey), args).withStyle(color),
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

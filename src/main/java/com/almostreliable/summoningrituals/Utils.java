package com.almostreliable.summoningrituals;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public final class Utils {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{}");
    private static final UUID MOD_UUID = UUID.nameUUIDFromBytes(BuildConfig.MOD_ID.getBytes());

    private Utils() {}

    /**
     * A helper method to format a string with simple bracket placeholders.
     * <p>
     * The bracket pairs will be replaced with the given values in the same order.
     * <p>
     * Brackets with index values will be replaced with the corresponding value.
     *
     * @param input the input string
     * @param args  the values to replace the brackets with
     * @return the formatted string
     */
    public static String f(String input, Object... args) {
        for (var arg : args) {
            input = PLACEHOLDER.matcher(input).replaceFirst(arg.toString());
        }
        for (var i = 0; i < args.length; i++) {
            input = input.replace("{" + i + "}", args[i].toString());
        }
        return input;
    }

    /**
     * Gets a resource location with the given key
     * and the namespace of the mod.
     *
     * @param key the key to generate the resource location with
     * @return the generated resource location
     */
    public static ResourceLocation getRL(String key) {
        return new ResourceLocation(BuildConfig.MOD_ID, key);
    }

    /**
     * Gets a translated phrase within the mod's namespace.
     *
     * @param type the translation type to get the translation from
     * @param key  the translation key
     * @return the translated phrase
     */
    public static String translateAsString(String type, String key) {
        return translate(type, key).getString();
    }

    /**
     * Generates a Translation Text Component within the mod's namespace
     * with a custom type, key and optional color.
     *
     * @param type  the type of the translation
     * @param key   the unique key of the translation
     * @param color an optional color
     * @return the translated phrase
     */
    public static TranslatableComponent translate(
        String type, String key, ChatFormatting... color
    ) {
        var output = new TranslatableComponent(getTranslationKey(type, key));
        return color.length == 0 ? output : (TranslatableComponent) output.withStyle(color[0]);
    }

    public static TranslatableComponent translateWithArgs(
        String type, String key, ChatFormatting color, Object... args
    ) {
        var output = new TranslatableComponent(getTranslationKey(type, key), args);
        return (TranslatableComponent) output.withStyle(color);
    }

    /**
     * Colors a given String with the given color.
     *
     * @param input the string to color
     * @param color an optional color
     * @return the colorized string
     */
    static TextComponent colorize(String input, ChatFormatting color) {
        return (TextComponent) new TextComponent(input).withStyle(color);
    }

    /**
     * Gets the translation key from the provided type and key.
     *
     * @param type the type of the translation
     * @param key  the unique key of the translation
     * @return the translation key
     */
    private static String getTranslationKey(String type, String key) {
        return String.format("%s.%s.%s", type.toLowerCase(), BuildConfig.MOD_ID, key);
    }

    public static void sendPlayerMessage(Player player, String translationKey, ChatFormatting color, Object... args) {
        player.sendMessage(
            translateWithArgs("message", translationKey, color, args),
            MOD_UUID
        );
    }

    public static RecipeManager getRecipeManager(@Nullable Level level) {
        if (level != null && level.getServer() != null) return level.getServer().getRecipeManager();
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return ServerLifecycleHooks.getCurrentServer().getRecipeManager();
        }
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getRecipeManager();
    }
}

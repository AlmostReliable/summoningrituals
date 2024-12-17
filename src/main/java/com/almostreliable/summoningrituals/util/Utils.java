package com.almostreliable.summoningrituals.util;

import com.almostreliable.summoningrituals.ModConstants;
import net.minecraft.resources.ResourceLocation;

public final class Utils {

    private Utils() {}

    public static ResourceLocation getRL(String key) {
        return ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }
}

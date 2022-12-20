package com.almostreliable.summoningrituals.util;

import com.almostreliable.summoningrituals.BuildConfig;
import net.minecraft.resources.ResourceLocation;

public final class Utils {

    private Utils() {}

    public static ResourceLocation getRL(String key) {
        return new ResourceLocation(BuildConfig.MOD_ID, key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }
}

package com.almostreliable.summoningrituals.recipe.codec;

import net.minecraft.network.codec.StreamCodec;

import com.mojang.datafixers.util.Function9;

import java.util.function.Function;

public final class CodecUtils {

    private CodecUtils() {}

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> StreamCodec<B, C> composite(
        StreamCodec<? super B, T1> codec1,
        Function<C, T1> getter1,
        StreamCodec<? super B, T2> codec2,
        Function<C, T2> getter2,
        StreamCodec<? super B, T3> codec3,
        Function<C, T3> getter3,
        StreamCodec<? super B, T4> codec4,
        Function<C, T4> getter4,
        StreamCodec<? super B, T5> codec5,
        Function<C, T5> getter5,
        StreamCodec<? super B, T6> codec6,
        Function<C, T6> getter6,
        StreamCodec<? super B, T7> codec7,
        Function<C, T7> getter7,
        StreamCodec<? super B, T8> codec8,
        Function<C, T8> getter8,
        StreamCodec<? super B, T9> codec9,
        Function<C, T9> getter9,
        Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                var t1 = codec1.decode(buffer);
                var t2 = codec2.decode(buffer);
                var t3 = codec3.decode(buffer);
                var t4 = codec4.decode(buffer);
                var t5 = codec5.decode(buffer);
                var t6 = codec6.decode(buffer);
                var t7 = codec7.decode(buffer);
                var t8 = codec8.decode(buffer);
                var t9 = codec9.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
                codec7.encode(buffer, getter7.apply(value));
                codec8.encode(buffer, getter8.apply(value));
                codec9.encode(buffer, getter9.apply(value));
            }
        };
    }
}

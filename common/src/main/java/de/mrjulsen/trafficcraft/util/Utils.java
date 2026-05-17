package de.mrjulsen.trafficcraft.util;

import com.mojang.datafixers.types.Func;
import net.minecraft.nbt.CompoundTag;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Utils {
    private static double calcScale(double minScale, double maxScale, double maxWidth, double fontWidth) {
        double scale = Math.min(maxWidth / fontWidth, 1.0D);
        return Math.max(maxScale * scale, minScale);
    }

    public static double getScale(float fontWidth, float lineWidth, float min, float max) {
        return calcScale(min, max, lineWidth / max, fontWidth);
    }   
    
    public static byte[] compress(byte[] data, int level) throws Exception {
        Deflater deflater = new Deflater(level);
        deflater.setInput(data);
        deflater.finish();
    
        byte[] buffer = new byte[1024];
        int compressedDataLength;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            while (!deflater.finished()) {
                compressedDataLength = deflater.deflate(buffer);
                outputStream.write(buffer, 0, compressedDataLength);
            }
            return outputStream.toByteArray();
        }
    }

    public static byte[] decompress(byte[] data) throws Exception {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        byte[] buffer = new byte[1024];
        int decompressedDataLength;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            while (!inflater.finished()) {
                decompressedDataLength = inflater.inflate(buffer);
                outputStream.write(buffer, 0, decompressedDataLength);
            }
            return outputStream.toByteArray();
        }
    }


    public static <K, V> Map<K, V> getMapWithKey(CompoundTag nbt, String key, Function<String, K> keyDeserializer, BiFunction<CompoundTag, K, V> valueDeserializer) {
        CompoundTag mapNbt = nbt.getCompound(key);
        Map<K, V> map = new HashMap<>(mapNbt.size());
        for (String k : mapNbt.getAllKeys()) {
            K dk = keyDeserializer.apply(k);
            map.put(dk, valueDeserializer.apply(mapNbt.getCompound(k), dk));
        }
        return map;
    }

    public static <T, S> void doIfType(T obj, Class<S> type, Consumer<S> action) {
        doIfType(obj, type, action, $ -> {});
    }

    public static <T, S> void doIfType(T obj, Class<S> type, Consumer<S> action, Consumer<T> ifNot) {
        if (type.isInstance(obj)) {
            action.accept(type.cast(obj));
        } else {
            ifNot.accept(obj);
        }
    }

    public static <T, S> void doIfNotType(T obj, Class<S> type, Consumer<T> action) {
        doIfType(obj, type, $ -> {}, action);
    }
}

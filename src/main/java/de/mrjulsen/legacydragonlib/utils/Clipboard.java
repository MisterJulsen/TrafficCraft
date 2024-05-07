package de.mrjulsen.legacydragonlib.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraft.nbt.CompoundTag;

public class Clipboard {

    protected static final Map<Class<? extends IClipboardData>, CompoundTag> clipboardData = new HashMap<>();

    public static <T extends IClipboardData> void put(Class<T> clipboardClass, T data) {
        if (clipboardData.containsKey(clipboardClass)) {
            clipboardData.remove(clipboardClass);
        }

        clipboardData.put(clipboardClass, data.serializeNbt());
    }

    public static <T extends IClipboardData> Optional<T> get(Class<T> clipboardClass) {
        if (clipboardData.containsKey(clipboardClass)) {
            try {                
                T t = clipboardClass.getDeclaredConstructor().newInstance();
                t.deserializeNbt(clipboardData.get(clipboardClass));
                return Optional.of(t);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                ModMain.LOGGER.error("Unable to deserialize clipbaord data.", e);
            }
        }
        return Optional.empty();
    }

    public static <T extends IClipboardData> boolean contains(Class<T> clipboardClass) {
        return clipboardData.containsKey(clipboardClass);
    }

    public static void clear() {
        clipboardData.clear();
    }

    public static <T extends IClipboardData> Optional<T> clear(Class<T> clipboardClass) {
        CompoundTag nbt = clipboardData.remove(clipboardClass);
        try {                
            T t = clipboardClass.getDeclaredConstructor().newInstance();
            t.deserializeNbt(nbt);
            return Optional.of(t);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            ModMain.LOGGER.error("Unable to deserialize clipbaord data.", e);
        }
        return Optional.empty();
    }
}


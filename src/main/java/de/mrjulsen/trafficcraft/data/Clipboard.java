package de.mrjulsen.trafficcraft.data;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraft.nbt.CompoundTag;

public final class Clipboard {
    public static final Map<Class<? extends IClipboardData>, CompoundTag> clipboardData = new HashMap<>();

    public static <T extends IClipboardData> void setToClipboard(Class<T> clipboardClass, T data) {
        if (clipboardData.containsKey(clipboardClass)) {
            clipboardData.remove(clipboardClass);
        }

        clipboardData.put(clipboardClass, data.serializeNbt());
    }

    public static <T extends IClipboardData> T getFromClipboard(Class<T> clipboardClass) {
        if (clipboardData.containsKey(clipboardClass)) {
            try {                
                T t = clipboardClass.getDeclaredConstructor().newInstance();
                t.deserializeNbt(clipboardData.get(clipboardClass));
                return t;
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                ModMain.LOGGER.error("Unable to deserialize clipbaord data.", e);
            }
        }
        return null;
    }
}

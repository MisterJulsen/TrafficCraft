package de.mrjulsen.trafficcraft.data.texture.decoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class TextureDecodeContext {

    private final Map<String, Object> values = new HashMap<>();

    public <T> TextureDecodeContext put(String key, T value) {
        values.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        return Optional.ofNullable((T) values.get(key));
    }

    @SuppressWarnings("unchecked")
    public <T> T require(String key) {
        T value = (T) values.get(key);
        if (value == null) throw new IllegalStateException("Required context key missing: " + key);
        return value;
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }
}
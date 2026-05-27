package de.mrjulsen.trafficcraft.data.textures;

import com.google.common.collect.ImmutableSet;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.data.textures.data.ITextureData;
import de.mrjulsen.trafficcraft.data.textures.decoder.context.IDecoderContext;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LocalTextureCache implements AutoCloseable {

    private final Map<TextureIdentifier, TextureHandle> cachedHandles = new ConcurrentHashMap<>();

    public TextureHandle getTexture(TextureIdentifier key, IDecoderContext ctx) {
        return getTexture(key, ctx, $ -> {});
    }

    public TextureHandle getTexture(TextureIdentifier key, IDecoderContext ctx, Consumer<TextureHandle> callback) {
        boolean b = cachedHandles.containsKey(key);
        TextureHandle handle = cachedHandles.computeIfAbsent(key, k -> ClientTextureCache.INSTANCE.getTexture(key, ctx, callback));
        if (b) DLUtils.doIfNotNull(callback, c -> c.accept(handle));
        return handle;
    }

    public TextureHandle getTexture(TextureIdentifier key, ITextureData data, IDecoderContext ctx) {
        return cachedHandles.computeIfAbsent(key, k -> ClientTextureCache.INSTANCE.getTexture(key, data, ctx));
    }

    public Set<TextureIdentifier> getCachedTextureKeys() {
        return ImmutableSet.copyOf(cachedHandles.keySet());
    }

    public void releaseTexture(TextureIdentifier key) {
        ClientTextureCache.INSTANCE.release(key);
        cachedHandles.remove(key);
    }

    public void releaseAll() {
        cachedHandles.keySet().forEach(de.mrjulsen.trafficcraft.data.textures.ClientTextureCache.INSTANCE::release);
        cachedHandles.clear();
    }

    @Override
    public void close() throws Exception {
        releaseAll();
    }
}

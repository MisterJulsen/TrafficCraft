package de.mrjulsen.trafficcraft.data.textures;

import com.mojang.logging.LogUtils;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.data.textures.decoder.context.IDecoderContext;
import de.mrjulsen.trafficcraft.data.textures.data.ITextureData;
import de.mrjulsen.trafficcraft.network.packets.cts.GetTexturePacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientTextureCache {

    public static final ClientTextureCache INSTANCE = new ClientTextureCache();
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<TextureIdentifier, CachedEntry> cache = new HashMap<>();

    private record CachedEntry(TextureHandle handle, AtomicInteger refCount) {}

    private ClientTextureCache() {}


/*
    public TextureHandle getTexture(TextureIdentifier id, IDecoderContext ctx) {
        return getTexture(id, ctx, null);
    }

    public TextureHandle getTexture(TextureIdentifier id, IDecoderContext ctx, Consumer<TextureHandle> callback) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(ctx);

        boolean b = cache.containsKey(id);
        CachedEntry entry = cache.computeIfAbsent(id, k -> {
            TextureHandle handle = new TextureHandle(k);
            getTextureDataAsync(id, data -> {
                try {
                    handle.init(data.orElseThrow(), ctx);
                } catch (Exception e) {
                    LOGGER.error("Failed to load texture: {}", id, e);
                }
                DLUtils.doIfNotNull(callback, c -> c.accept(handle));
            });
            return new CachedEntry(handle, new AtomicInteger(0));
        });
        entry.refCount().incrementAndGet();
        TextureHandle handle = entry.handle();
        if (b) DLUtils.doIfNotNull(callback, c -> c.accept(handle));
        return handle;
    }

    public TextureHandle getTexture(TextureIdentifier id, ITextureData data, IDecoderContext ctx) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(data);
        Objects.requireNonNull(ctx);

        CachedEntry entry = cache.computeIfAbsent(id, k -> {
            TextureHandle handle = new TextureHandle(k);
            try {
                handle.init(data, ctx);
            } catch (Exception e) {
                LOGGER.error("Failed to load texture: {}", id, e);
            }
            return new CachedEntry(handle, new AtomicInteger(0));
        });
        entry.refCount().incrementAndGet();
        return entry.handle();
    }

 */
    public TextureHandle getTexture(TextureIdentifier id, IDecoderContext ctx) {
        return getTexture(id, ctx, null);
    }

    public TextureHandle getTexture(TextureIdentifier id, IDecoderContext ctx, Consumer<TextureHandle> callback) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(ctx);

        boolean b = cache.containsKey(id);
        TextureHandle handle = getTextureInternal(id, (h) -> {
            getTextureDataAsync(id, data -> {
                try {
                    h.init(data.orElseThrow(), ctx);
                } catch (Exception e) {
                    LOGGER.error("Failed to load texture: {}", id, e);
                }
                DLUtils.doIfNotNull(callback, c -> c.accept(h));
            });
        });
        if (b) DLUtils.doIfNotNull(callback, c -> c.accept(handle));
        return handle;
    }

    public TextureHandle getTexture(TextureIdentifier id, ITextureData data, IDecoderContext ctx) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(ctx);

        return getTextureInternal(id, (handle) -> {
            try {
                handle.init(data, ctx);
            } catch (IOException e) {
                LOGGER.error("Failed to load texture: {}", id, e);
            }
        });
    }

    private TextureHandle getTextureInternal(TextureIdentifier id, Consumer<TextureHandle> initializer) {
        CachedEntry entry = cache.computeIfAbsent(id, k -> {
            TextureHandle handle = new TextureHandle(k);
            initializer.accept(handle);
            return new CachedEntry(handle, new AtomicInteger(0));
        });
        entry.refCount().incrementAndGet();
        return entry.handle();
    }



    public void release(TextureIdentifier id) {
        CachedEntry entry = cache.get(id);
        if (entry == null) return;
        if (entry.refCount().decrementAndGet() <= 0) {
            cache.remove(id);
            entry.handle().close();
        }
    }

    public int size() {
        return cache.size();
    }

    public int releaseAll() {
        int size = size();
        cache.values().forEach(entry -> entry.handle().close());
        cache.clear();
        return size;
    }

    /*
    private void loadAsync(TextureIdentifier id, TextureHandle handle, IDecoderContext ctx, Consumer<TextureHandle> callback) {
        if (!id.isCustom()) {
            loadBuiltIn(id, handle, ctx, callback);
        } else {
            loadCustom(id, handle, ctx, callback);
        }
    }

    private void loadBuiltIn(TextureIdentifier id, TextureHandle handle, IDecoderContext ctx, Consumer<TextureHandle> callback) {
        try {
            loadInternal(TextureDataManager.INSTANCE.get(new ResourceLocation(id.getLocation())).orElseThrow(), handle, ctx, callback);
        } catch (Exception e) {
            LOGGER.error("Failed to load built-in texture: {}", id, e);
        }
    }

    private void loadCustom(TextureIdentifier id, TextureHandle handle, IDecoderContext ctx, Consumer<TextureHandle> callback) {
        ModNetworkManager.GET_TEXTURE.send(
                NetworkDirection.toServer(),
                new GetTexturePacket.Request(id),
                response -> {
                    try {
                        loadInternal(response.getTextureData().orElseThrow(), handle, ctx, callback);
                    } catch (Exception e) {
                        LOGGER.error("Failed to load built-in texture: {}", id, e);
                    }
                },
                () -> LOGGER.warn("Texture request timed out: {}", id)
        );
    }

     */




    public void getTextureDataAsync(TextureIdentifier id, Consumer<Optional<ITextureData>> callback) {
        if (!id.isCustom()) {
            callback.accept(getBuiltInTextureData(id));
        } else {
            getCustomTextureDataAsync(id, callback);
        }
    }

    public Optional<ITextureData> getBuiltInTextureData(TextureIdentifier id) {
        try {
            return TextureDataManager.INSTANCE.get(new ResourceLocation(id.getLocation()));
        } catch (Exception e) {
            LOGGER.error("Failed to load built-in texture: {}", id, e);
        }
        return Optional.empty();
    }

    public void getCustomTextureDataAsync(TextureIdentifier id, Consumer<Optional<ITextureData>> callback) {
        ModNetworkManager.GET_TEXTURE.send(
                NetworkDirection.toServer(),
                new GetTexturePacket.Request(id),
                response -> {
                    try {
                        callback.accept((Optional<ITextureData>)(Object)response.getTextureData());
                    } catch (Exception e) {
                        LOGGER.error("Failed to load built-in texture: {}", id, e);
                        callback.accept(Optional.empty());
                    }
                },
                () -> {
                    LOGGER.warn("Texture request timed out: {}", id);
                    callback.accept(Optional.empty());
                }
        );
    }
}
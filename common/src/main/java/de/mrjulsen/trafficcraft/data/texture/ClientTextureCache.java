package de.mrjulsen.trafficcraft.data.texture;

import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.network.packets.cts.GetTexturePacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientTextureCache {

    public static final ClientTextureCache INSTANCE = new ClientTextureCache();

    private final Map<TextureKey, Entry> cache = new HashMap<>();

    private record Entry(ClientTextureHandle handle, AtomicInteger refCount) {}

    private ClientTextureCache() {}

    public ClientTextureHandle acquire(TextureKey key, Runnable afterLoad) {
        Entry entry = cache.computeIfAbsent(key, k -> {
            ClientTextureHandle handle = new ClientTextureHandle(k);
            loadAsync(k, handle, afterLoad);
            return new Entry(handle, new AtomicInteger(0));
        });
        entry.refCount().incrementAndGet();
        return entry.handle();
    }

    public void release(TextureKey key) {
        Entry entry = cache.get(key);
        if (entry == null) {
            TrafficCraft.LOGGER.warn("release() called for unknown texture key: {}", key);
            return;
        }
        if (entry.refCount().decrementAndGet() <= 0) {
            cache.remove(key);
            entry.handle().close();
        }
    }

    public void releaseAll() {
        new ArrayList<>(cache.values()).forEach(e -> e.handle().close());
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    private void loadAsync(TextureKey key, ClientTextureHandle handle, Runnable afterLoad) {
        Optional<ITextureCodec> codecOpt = DynamicTextureRegistry.getTextureCodec(key);

        if (key.isBuiltIn()) {
            codecOpt.ifPresentOrElse(codec -> {
                ITexturePayload payload = codec.decodeBuiltIn(key);
                handle.init(payload, codec);
                DLUtils.doIfNotNull(afterLoad, Runnable::run);
            }, () -> TrafficCraft.LOGGER.error("No codec for built-in key: {}", key));
            return;
        }

        ModNetworkManager.GET_TEXTURE.send(
                NetworkDirection.toServer(),
                new GetTexturePacket.Request(key),
                (response) -> {
                    ITexturePayload payload = response.getPayload();
                    codecOpt.ifPresentOrElse(codec -> {
                        handle.init(payload, codec);
                        DLUtils.doIfNotNull(afterLoad, Runnable::run);
                    }, () -> TrafficCraft.LOGGER.error("No codec for custom key: {}", key));
                },
                () -> {}
        );
    }
}
package de.mrjulsen.trafficcraft.data.texture;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.SafeDynamicTexture;
import de.mrjulsen.trafficcraft.data.texture.decoder.TextureDecodeContext;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientTextureHandle implements AutoCloseable {

    private final TextureKey key;
    private ResourceLocation location = TrafficCraft.EMPTY_LOCATION;
    private SafeDynamicTexture texture = TrafficCraft.EMPTY_TEXTURE;
    private final Map<String, ClientTextureHandle> secondaryHandles = new HashMap<>();
    private boolean closed = false;
    private boolean loaded = false;

    public ClientTextureHandle(TextureKey key) {
        this.key = key;
    }

    public synchronized void init(ITexturePayload payload, ITextureCodec codec) {
        if (closed) return;
        try {
            IPayloadDecoder decoder = DynamicTextureRegistry.getTextureDecoder(payload.getDecoder().getDecoderId()).orElseThrow(() -> new RuntimeException(String.format("No decoder found for %s.", payload.getDecoder().getDecoderId())));
            TextureDecodeContext context = codec.buildDecodeContext(key, payload);
            NativeImage image = decoder.decode(payload, context);

            if (image != null) {
                this.texture = new SafeDynamicTexture(image);
                this.location = new ResourceLocation(TrafficCraft.MOD_ID, "tex_" + payload.getHash());
                Minecraft.getInstance().getTextureManager().register(location, texture);
            }

            codec.onHandleInit(this, key, payload);

            this.loaded = true;
        } catch (Exception e) {
            TrafficCraft.LOGGER.error("Failed to init ClientTextureHandle for key: {}", key, e);
        }
    }

    public void registerSecondary(String name, ClientTextureHandle handle) {
        secondaryHandles.put(name, handle);
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public SafeDynamicTexture getTexture() {
        return texture;
    }

    public Optional<ClientTextureHandle> getSecondary(String name) {
        return Optional.ofNullable(secondaryHandles.get(name));
    }

    public Map<String, ClientTextureHandle> getAllSecondary() {
        return Collections.unmodifiableMap(secondaryHandles);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isClosed() {
        return closed;
    }

    public TextureKey getKey() {
        return key;
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        if (texture != TrafficCraft.EMPTY_TEXTURE) {
            Minecraft.getInstance().getTextureManager().release(location);
            texture.close();
        }
        secondaryHandles.values().forEach(ClientTextureHandle::close);
    }
}
package de.mrjulsen.trafficcraft.data.textures;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.SafeDynamicTexture;
import de.mrjulsen.trafficcraft.data.textures.data.ITextureData;
import de.mrjulsen.trafficcraft.data.textures.data.TextureSource;
import de.mrjulsen.trafficcraft.data.textures.decoder.ITextureDecoder;
import de.mrjulsen.trafficcraft.data.textures.decoder.context.IDecoderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.Optional;

public class TextureHandle implements AutoCloseable {

    public static final TextureHandle EMPTY = new TextureHandle(null) {{
        loaded = true;
    }};

    private final TextureIdentifier key;
    private ResourceLocation registeredLocation = TrafficCraft.EMPTY_LOCATION;
    private SafeDynamicTexture texture = TrafficCraft.EMPTY_TEXTURE;
    private ITextureData data;
    private TextureHandleExtension extension = null;

    private boolean closed = false;
    boolean loaded = false;

    public TextureHandle(TextureIdentifier key) {
        this.key = key;
    }

    public synchronized void init(ITextureData data, IDecoderContext ctx) throws IOException {
        if (closed) return;

        boolean needsImage = data.getType().decoder().get().requiresNativeImage();
        TextureSource src = data.getSource();

        if (src instanceof TextureSource.Custom custom) {
            byte[] bytes = custom.bytes();
            int w = custom.width();
            int h = custom.height();

            NativeImage image = decodeImage(data, new RawTextureData(TextureSource.Type.CUSTOM, bytes, w, h), ctx);
            this.texture = new SafeDynamicTexture(image);
            this.registeredLocation = buildDynLocation(key);
            Minecraft.getInstance().getTextureManager().register(registeredLocation, texture);
        } else if (src instanceof TextureSource.BuiltIn builtIn) {
            ResourceLocation relativeTextureLocation = builtIn.location();
            ResourceLocation textureLocation = DLUtils.resourceLocation(relativeTextureLocation.getNamespace(), data.getType().decoder().get().generateTexturePath(relativeTextureLocation.getPath()));

            if (needsImage) {
                NativeImage image = decodeImage(data, RawTextureData.fromResource(textureLocation), ctx);
                this.texture = new SafeDynamicTexture(image);
                this.registeredLocation = buildDynLocation(key);
                Minecraft.getInstance().getTextureManager().register(registeredLocation, texture);
            } else {
                this.registeredLocation = textureLocation;
            }
        }
        this.data = data;

        this.extension = data.createExtension().orElse(null);
        if (extension != null) {
            extension.onInit(data, this, ctx);
        }

        this.loaded = true;
    }

    private static ResourceLocation buildDynLocation(TextureIdentifier key) {
        return new ResourceLocation(TrafficCraft.MOD_ID, "dynamic_texture/" + key.getTypeId().getPath().replace(":", "_") + "/" + key.getLocation().replace(":", "_"));
    }

    @SuppressWarnings("unchecked")
    private static NativeImage decodeImage(ITextureData data, RawTextureData raw, IDecoderContext ctx) throws IOException {
        ITextureDecoder<IDecoderContext> decoder = (ITextureDecoder<IDecoderContext>) data.getType().decoder().get();
        return decoder.decode(raw, ctx);
    }

    public <E extends TextureHandleExtension> Optional<E> getExtension(Class<E> type) {
        return type.isInstance(extension) ? Optional.of(type.cast(extension)) : Optional.empty();
    }

    public ResourceLocation getLocation() {
        return registeredLocation;
    }

    public SafeDynamicTexture getTexture() {
        return texture;
    }

    public ITextureData getData() {
        return data;
    }

    public <T extends ITextureData> Optional<T> getTextureData(Class<T> type) {
        return ITextureData.ifType(type, getData());
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isClosed() {
        return closed;
    }

    public TextureIdentifier getKey() {
        return key;
    }

    @Override
    public void close() {
        if (closed || this == EMPTY) return;
        closed = true;
        if (texture != TrafficCraft.EMPTY_TEXTURE) {
            Minecraft.getInstance().getTextureManager().release(registeredLocation);
            texture.close();
        }
        if (extension != null) {
            extension.close();
        }
    }
}
package de.mrjulsen.trafficcraft.data.texture;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

public class DynamicTextureRegistry {
    private DynamicTextureRegistry() {}

    public record RegisteredTextureDecoder<T extends IPayloadDecoder>(ResourceLocation id, T decoder) {}
    public record RegisteredTextureCodec<T extends ITextureCodec>(ResourceLocation id, T codec) {}

    private static final Map<ResourceLocation, IPayloadDecoder> decoderRegistry = new HashMap<>();
    private static final Map<ResourceLocation, ITextureCodec> textureCodecRegistry = new HashMap<>();

    public static <T extends IPayloadDecoder> RegisteredTextureDecoder<T> registerTextureDecoder(T decoder) {
        if (decoderRegistry.containsKey(decoder.getDecoderId())) {
            throw new IllegalStateException("Cannot register decoder for " + decoder.getDecoderId());
        }
        decoderRegistry.put(decoder.getDecoderId(), decoder);
        return new RegisteredTextureDecoder<>(decoder.getDecoderId(), decoder);
    }

    public static <T extends ITextureCodec> RegisteredTextureCodec<T> registerTextureCodec(T codec) {
        if (textureCodecRegistry.containsKey(codec.getTypeId())) {
            throw new IllegalStateException("Cannot register texture codec for " + codec.getTypeId());
        }
        textureCodecRegistry.put(codec.getTypeId(), codec);
        return new RegisteredTextureCodec<>(codec.getTypeId(), codec);
    }



    public static Optional<IPayloadDecoder> getTextureDecoder(ResourceLocation id) {
        return Optional.ofNullable(decoderRegistry.get(id));
    }

    public static <T extends IPayloadDecoder> Optional<T> getTextureDecoder(ResourceLocation id, Class<T> clazz) {
        if (clazz.isInstance(decoderRegistry.get(id))) {
            return Optional.of(clazz.cast(decoderRegistry.get(id)));
        }
        return Optional.empty();
    }

    public static Optional<ITextureCodec> getTextureCodec(TextureKey key) {
        return Optional.ofNullable(textureCodecRegistry.get(key.getTypeId()));
    }
}

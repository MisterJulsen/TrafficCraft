package de.mrjulsen.trafficcraft.data.textures;

import de.mrjulsen.trafficcraft.data.textures.data.ITextureData;
import de.mrjulsen.trafficcraft.data.textures.decoder.context.IDecoderContext;

public interface TextureHandleExtension extends AutoCloseable {

    void onInit(ITextureData data, TextureHandle owner, IDecoderContext ctx);

    @Override
    default void close() {}
}
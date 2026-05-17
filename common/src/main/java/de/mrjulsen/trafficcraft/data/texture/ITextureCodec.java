package de.mrjulsen.trafficcraft.data.texture;

import de.mrjulsen.trafficcraft.data.texture.decoder.TextureDecodeContext;
import net.minecraft.resources.ResourceLocation;

public interface ITextureCodec {
    ResourceLocation getTypeId();
    DynamicTextureRegistry.RegisteredTextureDecoder<?> getDecoder();

    ITexturePayload decodeBuiltIn(TextureKey key);
    TextureDecodeContext buildDecodeContext(TextureKey key, ITexturePayload payload);

    default void onHandleInit(ClientTextureHandle handle, TextureKey key, ITexturePayload payload) {
    }
}
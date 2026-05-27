package de.mrjulsen.trafficcraft.data.textures.decoder;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.trafficcraft.data.textures.RawTextureData;
import de.mrjulsen.trafficcraft.data.textures.decoder.context.IDecoderContext;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public interface ITextureDecoder<T extends IDecoderContext> {

    ResourceLocation getId();
    NativeImage decode(RawTextureData data, T context) throws IOException;
    boolean requiresNativeImage();

    default String generateTexturePath(String path) {
        return "textures/" + path + ".png";
    }
}

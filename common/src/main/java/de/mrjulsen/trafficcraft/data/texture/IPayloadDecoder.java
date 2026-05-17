package de.mrjulsen.trafficcraft.data.texture;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.trafficcraft.data.texture.decoder.TextureDecodeContext;
import net.minecraft.resources.ResourceLocation;

public interface IPayloadDecoder {
    ResourceLocation getDecoderId();
    NativeImage decode(ITexturePayload payload, TextureDecodeContext context) throws Exception;
}
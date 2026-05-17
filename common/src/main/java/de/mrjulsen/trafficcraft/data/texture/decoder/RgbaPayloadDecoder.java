package de.mrjulsen.trafficcraft.data.texture.decoder;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.texture.IPayloadDecoder;
import de.mrjulsen.trafficcraft.data.texture.ITexturePayload;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;

public final class RgbaPayloadDecoder implements IPayloadDecoder {

    public static final ResourceLocation RGBA = new ResourceLocation(TrafficCraft.MOD_ID, "rgba_texture");

    @Override
    public ResourceLocation getDecoderId() {
        return RGBA;
    }

    @Override
    public NativeImage decode(ITexturePayload payload, TextureDecodeContext context) throws Exception {
        if (payload.getData().length == 0) return null;
        return NativeImage.read(new ByteArrayInputStream(payload.getData()));
    }
}
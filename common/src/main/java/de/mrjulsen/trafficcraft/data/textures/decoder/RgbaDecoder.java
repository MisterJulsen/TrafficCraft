package de.mrjulsen.trafficcraft.data.textures.decoder;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.textures.RawTextureData;
import de.mrjulsen.trafficcraft.data.textures.decoder.context.IDecoderContext;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class RgbaDecoder implements ITextureDecoder<IDecoderContext.Empty> {

    @Override
    public ResourceLocation getId() {
        return DLUtils.resourceLocation(TrafficCraft.MOD_ID, "rgba");
    }

    @Override
    public NativeImage decode(RawTextureData rawData, IDecoderContext.Empty context) throws IOException {
        return NativeImage.read(new ByteArrayInputStream(rawData.data()));
    }

    @Override
    public boolean requiresNativeImage() {
        return false;
    }
}

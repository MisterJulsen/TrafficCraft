package de.mrjulsen.trafficcraft.data.textures.decoder;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.textures.RawTextureData;
import de.mrjulsen.trafficcraft.data.textures.data.TextureSource;
import de.mrjulsen.trafficcraft.data.textures.decoder.context.BitmaskContext;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BitmaskDecoder implements ITextureDecoder<BitmaskContext> {

    @Override
    public ResourceLocation getId() {
        return DLUtils.resourceLocation(TrafficCraft.MOD_ID, "bitmask");
    }

    @Override
    public NativeImage decode(RawTextureData rawData, BitmaskContext context) throws IOException {
        ResourceLocation baseLocation = context.textureLocation();
        NativeImage target = NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(baseLocation).get().open());

        if (rawData.source() == TextureSource.Type.CUSTOM) {
            if (rawData.data().length > 0) {
                applyBitmask(target, rawData.data(), rawData.width());
            }
        } else {
            applyGrayscaleMask(target, rawData.data());
        }

        return target;
    }

    @Override
    public boolean requiresNativeImage() {
        return true;
    }

    @Override
    public String generateTexturePath(String path) {
        return "textures/" + path + ".png";
    }

    public static void applyBitmask(NativeImage target, byte[] mask, int width) {
        for (int y = 0; y < target.getHeight(); y++) {
            for (int x = 0; x < target.getWidth(); x++) {
                int index = y * width + x;
                boolean keep = (mask[index / 8] >> (index % 8) & 1) == 1;
                if (!keep) {
                    target.setPixelRGBA(x, y, 0x00000000);
                }
            }
        }
    }

    public static void applyGrayscaleMask(NativeImage target, byte[] pngBytes) throws IOException {
        try (NativeImage mask = NativeImage.read(NativeImage.Format.LUMINANCE, new ByteArrayInputStream(pngBytes))) {
            for (int y = 0; y < target.getHeight(); y++) {
                for (int x = 0; x < target.getWidth(); x++) {
                    int brightness = Byte.toUnsignedInt(mask.getRedOrLuminance(x, y));
                    if (brightness < 128) {
                        target.setPixelRGBA(x, y, 0x00000000);
                    }
                }
            }
        }
    }
}

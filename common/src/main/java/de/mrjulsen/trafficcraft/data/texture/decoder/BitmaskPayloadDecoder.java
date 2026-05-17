package de.mrjulsen.trafficcraft.data.texture.decoder;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.texture.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public final class BitmaskPayloadDecoder implements IPayloadDecoder {

    public static final String CTX_BASE_LOCATION = "base_location";
    public static final ResourceLocation BITMASK = new ResourceLocation(TrafficCraft.MOD_ID, "bitmask");

    @Override
    public ResourceLocation getDecoderId() {
        return BITMASK;
    }

    @Override
    public NativeImage decode(ITexturePayload payload, TextureDecodeContext context) throws Exception {
        ResourceLocation baseLocation = context.require(CTX_BASE_LOCATION);
        NativeImage img = NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(baseLocation).get().open());
        if (payload.getData().length > 0) {
            applyBitmask(img, payload.getData(), payload.getWidth());
        }
        return img;
    }

    public static byte[] generateBitmask(NativeImage image) {
        int total = image.getWidth() * image.getHeight();
        byte[] mask = new byte[(total + 7) / 8];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int index = y * image.getWidth() + x;
                int alpha = (image.getPixelRGBA(x, y) >> 24) & 0xFF;
                if (alpha > 0) {
                    mask[index / 8] |= (byte)(1 << (index % 8));
                }
            }
        }
        return mask;
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
}
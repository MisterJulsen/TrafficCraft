package de.mrjulsen.trafficcraft.data.texture.codec;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.texture.*;
import de.mrjulsen.trafficcraft.data.texture.decoder.BitmaskPayloadDecoder;
import de.mrjulsen.trafficcraft.data.texture.decoder.TextureDecodeContext;
import de.mrjulsen.trafficcraft.registry.ModClientRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class TrafficLightMaskCodec implements ITextureCodec {

    public static final ResourceLocation TYPE_ID = new ResourceLocation(TrafficCraft.MOD_ID, "traffic_light");

    @Override
    public ResourceLocation getTypeId() {
        return TYPE_ID;
    }

    @Override
    public DynamicTextureRegistry.RegisteredTextureDecoder<?> getDecoder() {
        return ModClientRegistries.BITMASK;
    }

    @Override
    public ITexturePayload decodeBuiltIn(TextureKey key) {
        return new TexturePayload(
                ModClientRegistries.BITMASK.decoder(), new byte[0],
                (short) 32, (short) 32,
                System.currentTimeMillis(), new UUID(0, 0), null
        );
    }

    @Override
    public TextureDecodeContext buildDecodeContext(TextureKey key, ITexturePayload payload) {
        return new TextureDecodeContext()
                .put(BitmaskPayloadDecoder.CTX_BASE_LOCATION, key.asBuiltInLocation());
    }
}
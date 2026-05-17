package de.mrjulsen.trafficcraft.registry;

import de.mrjulsen.trafficcraft.data.texture.DynamicTextureRegistry;
import de.mrjulsen.trafficcraft.data.texture.codec.TrafficLightMaskCodec;
import de.mrjulsen.trafficcraft.data.texture.codec.TrafficSignCodec;
import de.mrjulsen.trafficcraft.data.texture.decoder.BitmaskPayloadDecoder;
import de.mrjulsen.trafficcraft.data.texture.decoder.RgbaPayloadDecoder;

public final class ModClientRegistries {
    private ModClientRegistries() {}

    public static final DynamicTextureRegistry.RegisteredTextureDecoder<BitmaskPayloadDecoder> RGBA_TEXTURE = DynamicTextureRegistry.registerTextureDecoder(new BitmaskPayloadDecoder());
    public static final DynamicTextureRegistry.RegisteredTextureDecoder<RgbaPayloadDecoder> BITMASK = DynamicTextureRegistry.registerTextureDecoder(new RgbaPayloadDecoder());

    public static final DynamicTextureRegistry.RegisteredTextureCodec<TrafficSignCodec> TRAFFIC_SIGN_CODEC = DynamicTextureRegistry.registerTextureCodec(new TrafficSignCodec());
    public static final DynamicTextureRegistry.RegisteredTextureCodec<TrafficLightMaskCodec> TRAFFIC_LIGHT_CODEC = DynamicTextureRegistry.registerTextureCodec(new TrafficLightMaskCodec());


    public static void init() {

    }
}

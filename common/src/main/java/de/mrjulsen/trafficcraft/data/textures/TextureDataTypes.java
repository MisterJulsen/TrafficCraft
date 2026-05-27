package de.mrjulsen.trafficcraft.data.textures;

import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.textures.data.TextureDataType;
import de.mrjulsen.trafficcraft.data.textures.data.TrafficSignData;
import de.mrjulsen.trafficcraft.data.textures.decoder.ITextureDecoder;
import de.mrjulsen.trafficcraft.data.textures.decoder.BitmaskDecoder;
import de.mrjulsen.trafficcraft.data.textures.decoder.RgbaDecoder;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class TextureDataTypes {

    public static final ResourceKey<Registry<ITextureDecoder<?>>> TEXTURE_DECODER_REGISTRY = ResourceKey.createRegistryKey(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "texture_decoder"));
    public static final ResourceKey<Registry<TextureDataType<?>>> TEXTURE_DATA_TYPE_REGISTRY = ResourceKey.createRegistryKey(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "texture_data_type"));

    public static final DeferredRegister<ITextureDecoder<?>> DECODER_REGISTER = DeferredRegister.create(TrafficCraft.MOD_ID, TEXTURE_DECODER_REGISTRY);
    public static final DeferredRegister<TextureDataType<?>> TEXTURE_REGISTER = DeferredRegister.create(TrafficCraft.MOD_ID, TEXTURE_DATA_TYPE_REGISTRY);

    public static final RegistrySupplier<RgbaDecoder> RGBA_DECODER = DECODER_REGISTER.register("rgba", RgbaDecoder::new);
    public static final RegistrySupplier<BitmaskDecoder> BITMASK_DECODER = DECODER_REGISTER.register("bitmask", BitmaskDecoder::new);

    public static final RegistrySupplier<TextureDataType<TrafficSignData>> TRAFFIC_SIGN = TEXTURE_REGISTER.register("traffic_sign", () -> new TextureDataType<>(TrafficSignData.CODEC, RGBA_DECODER));


    public static void createRegistries() {
        RegistrarManager manager = RegistrarManager.get(TrafficCraft.MOD_ID);
        manager.builder(TEXTURE_DECODER_REGISTRY.location()).build();
        manager.builder(TEXTURE_DATA_TYPE_REGISTRY.location()).build();
    }

    public static void init() {
        createRegistries();
        DECODER_REGISTER.register();
        TEXTURE_REGISTER.register();
    }
}
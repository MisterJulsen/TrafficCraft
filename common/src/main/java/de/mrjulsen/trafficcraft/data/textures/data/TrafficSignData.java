package de.mrjulsen.trafficcraft.data.textures.data;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.SafeDynamicTexture;
import de.mrjulsen.trafficcraft.data.textures.TextureDataTypes;
import de.mrjulsen.trafficcraft.data.textures.TextureHandle;
import de.mrjulsen.trafficcraft.data.textures.TextureHandleExtension;
import de.mrjulsen.trafficcraft.data.textures.decoder.context.IDecoderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.Optional;

public record TrafficSignData(ResourceLocation modelLocation, ResourceLocation textureLocation, String name, String description, String category, boolean requiresBackground, ResourceLocation backgroundTextureLocation) implements ITextureData {

    public static final ResourceLocation EMPTY_MODEL = DLUtils.resourceLocation("empty");
    public static final ResourceLocation DEFAULT_BACKGROUND_TEXTURE = DLUtils.resourceLocation(TrafficCraft.MOD_ID, "block/sign/blank");

    public static final Codec<TrafficSignData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("model", EMPTY_MODEL).forGetter(TrafficSignData::modelLocation),
                    ResourceLocation.CODEC.optionalFieldOf("texture", MissingTextureAtlasSprite.getLocation()).forGetter(TrafficSignData::textureLocation),
                    Codec.STRING.optionalFieldOf("name", "").forGetter(TrafficSignData::name),
                    Codec.STRING.optionalFieldOf("description", "").forGetter(TrafficSignData::description),
                    Codec.STRING.optionalFieldOf("category", "").forGetter(TrafficSignData::category),
                    Codec.BOOL.optionalFieldOf("has_background", false).forGetter(TrafficSignData::requiresBackground),
                    ResourceLocation.CODEC.optionalFieldOf("background_texture", DEFAULT_BACKGROUND_TEXTURE).forGetter(TrafficSignData::backgroundTextureLocation)
            ).apply(instance, TrafficSignData::new)
    );

    @Override
    public TextureDataType<TrafficSignData> getType() {
        return TextureDataTypes.TRAFFIC_SIGN.get();
    }

    @Override
    public TextureSource getSource() {
        return new TextureSource.BuiltIn(textureLocation);
    }

    @Override
    public Optional<TextureHandleExtension> createExtension() {
        return Optional.of(new TrafficSignExtension());
    }



    public static class TrafficSignExtension implements TextureHandleExtension {

        private ResourceLocation backLocation = TrafficCraft.EMPTY_LOCATION;
        private SafeDynamicTexture backTexture = TrafficCraft.EMPTY_TEXTURE;

        @Override
        public void onInit(ITextureData data, TextureHandle owner, IDecoderContext ctx) {
            TrafficSignData signData = (TrafficSignData)data;

            if (!signData.requiresBackground()) return;

            try {
                ResourceLocation blankLoc = DLUtils.resourceLocation(signData.backgroundTextureLocation().getNamespace(), "textures/" + signData.backgroundTextureLocation().getPath() + ".png");
                NativeImage blank = NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(blankLoc).orElseThrow().open());
                NativeImage front = NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(owner.getLocation()).orElseThrow().open());

                applyFrontMask(blank, front);
                front.close();

                backTexture = new SafeDynamicTexture(blank);
                backLocation = new ResourceLocation(TrafficCraft.MOD_ID, "dynamic_texture/sign_back/" + owner.getKey().getLocation().replace(":", "_"));
                Minecraft.getInstance().getTextureManager().register(backLocation, backTexture);
            } catch (IOException e) {
                TrafficCraft.LOGGER.error("Failed to generate back texture", e);
            }
        }

        private static void applyFrontMask(NativeImage target, NativeImage mask) {
            for (int y = 0; y < target.getHeight(); y++) {
                for (int x = 0; x < target.getWidth(); x++) {
                    int alpha = (mask.getPixelRGBA(x, y) >> 24) & 0xFF;
                    if (alpha == 0) {
                        target.setPixelRGBA(x, y, 0x00000000);
                    }
                }
            }
        }

        public Optional<ResourceLocation> getBackLocation() {
            return Optional.ofNullable(backLocation);
        }

        @Override
        public void close() {
            if (backTexture != null && backTexture != TrafficCraft.EMPTY_TEXTURE) {
                Minecraft.getInstance().getTextureManager().release(backLocation);
                backTexture.close();
            }
        }
    }
}
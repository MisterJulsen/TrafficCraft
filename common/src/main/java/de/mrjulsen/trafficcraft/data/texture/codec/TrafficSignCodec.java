package de.mrjulsen.trafficcraft.data.texture.codec;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.data.texture.*;
import de.mrjulsen.trafficcraft.data.texture.decoder.TextureDecodeContext;
import de.mrjulsen.trafficcraft.registry.ModClientRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

public class TrafficSignCodec implements ITextureCodec {

    public static final String SECONDARY_BACKGROUND = "background";

    private static final String NBT_SHAPE = "Shape";

    @Override
    public ResourceLocation getTypeId() {
        return new ResourceLocation(TrafficCraft.MOD_ID, "sign");
    }

    @Override
    public DynamicTextureRegistry.RegisteredTextureDecoder<?> getDecoder() {
        return ModClientRegistries.RGBA_TEXTURE;
    }

    @Override
    public ITexturePayload decodeBuiltIn(TextureKey key) {
        return ITexturePayload.empty();
    }

    @Override
    public TextureDecodeContext buildDecodeContext(TextureKey key, ITexturePayload payload) {
        return new TextureDecodeContext();
    }

    @Override
    public void onHandleInit(ClientTextureHandle handle, TextureKey key, ITexturePayload payload) {
        TrafficSignShape shape = readShape(payload);
        if (shape != TrafficSignShape.MISC) return;

        ClientTextureHandle bgHandle = new ClientTextureHandle(key);

        NativeImage bg;
        try {
            bg = NativeImage.read(
                    Minecraft.getInstance().getResourceManager()
                            .getResource(new ResourceLocation(TrafficCraft.MOD_ID, "textures/block/sign/blank.png"))
                            .get().open()
            );
        } catch (IOException e) {
            TrafficCraft.LOGGER.error("Failed to load background base texture.", e);
            handle.registerSecondary(SECONDARY_BACKGROUND, bgHandle);
            return;
        }

        NativeImage src;
        try {
            src = NativeImage.read(new ByteArrayInputStream(payload.getData()));
        } catch (IOException e) {
            TrafficCraft.LOGGER.error("Failed to read sign pixel data for background.", e);
            bg.close();
            handle.registerSecondary(SECONDARY_BACKGROUND, bgHandle);
            return;
        }

        int width = Math.min(bg.getWidth(), TrafficSignShape.MAX_WIDTH);
        int height = Math.min(bg.getHeight(), TrafficSignShape.MAX_HEIGHT);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (src.getPixelRGBA(x, y) != 0) continue;
                bg.setPixelRGBA(x, y, 0);
            }
        }
        src.close();

        byte[] data = new byte[width * height];
        try {
            data = bg.asByteArray();
        } catch (IOException e) {
            TrafficCraft.LOGGER.error("Failed to read sign pixel data for background.", e);
        }

        bgHandle.init(
                new TexturePayload(
                        ModClientRegistries.RGBA_TEXTURE.decoder(),
                        data,
                        (short) bg.getWidth(), (short) bg.getHeight(),
                        System.currentTimeMillis(), new UUID(0, 0), null
                ),
                this
        );

        handle.registerSecondary(SECONDARY_BACKGROUND, bgHandle);
    }

    public static CompoundTag encodeShape(TrafficSignShape shape) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(NBT_SHAPE, shape.getIndex());
        return nbt;
    }

    public static TrafficSignShape readShape(ITexturePayload payload) {
        return TrafficSignShape.getShapeByIndex(payload.getCustomData().getInt(NBT_SHAPE));
    }
}
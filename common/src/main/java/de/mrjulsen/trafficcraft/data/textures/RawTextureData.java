package de.mrjulsen.trafficcraft.data.textures;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.textures.data.TextureSource;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public record RawTextureData(TextureSource.Type source, byte[] data, int width, int height) {
    public static final RawTextureData EMPTY = new RawTextureData(TextureSource.Type.CUSTOM, new byte[1], 1, 1);

    public static RawTextureData fromResource(ResourceLocation location) {
        return Minecraft.getInstance().getResourceManager().getResource(location)
                .map(resource -> {
                    try {
                        byte[] bytes = resource.open().readAllBytes();
                        ByteBuffer buf = MemoryUtil.memAlloc(bytes.length);
                        try {
                            buf.put(bytes).rewind();
                            try (MemoryStack stack = MemoryStack.stackPush()) {
                                IntBuffer w = stack.mallocInt(1);
                                IntBuffer h = stack.mallocInt(1);
                                IntBuffer channels = stack.mallocInt(1);
                                STBImage.stbi_info_from_memory(buf, w, h, channels);
                                return new RawTextureData(TextureSource.Type.BUILT_IN, bytes, w.get(0), h.get(0));
                            }
                        } finally {
                            MemoryUtil.memFree(buf);
                        }
                    } catch (IOException e) {
                        TrafficCraft.LOGGER.error("Failed to load texture at {}.", location, e);
                        return EMPTY;
                    }
                }).orElse(EMPTY);
    }
}

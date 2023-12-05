package de.mrjulsen.trafficcraft.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.NativeImage;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.data.IIdentifiable;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class TrafficSignTextureCacheClient {
    public static final HashMap<String, DynamicTexture> textureCache = new HashMap<>();
    public static final HashMap<String, DynamicTexture> backgroundTextureCache = new HashMap<>();

    public static <B extends IIdentifiable> DynamicTexture setTexture(B id, boolean hasBg, DynamicTexture texture) {
        clear(id);
        textureCache.put(id.getId(), texture);
    
        if (texture != null && hasBg) {
            try {
                NativeImage bg = NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(ModMain.MOD_ID, "textures/block/sign/blank.png")).getInputStream());
                for (int x = 0; x < TrafficSignShape.MAX_WIDTH; x++) {
                    for (int y = 0; y < TrafficSignShape.MAX_HEIGHT; y++) {
                        if (texture.getPixels().getPixelRGBA(x, y) != 0)
                            continue;

                        bg.setPixelRGBA(TrafficSignShape.MAX_WIDTH - 1 - x, y, 0);
                    }
                }
                backgroundTextureCache.put(id.getId(), new DynamicTexture(bg));
            } catch (IOException e) {
                ModMain.LOGGER.error("Unable to set dynamic texture.", e);
                e.printStackTrace();
            }            
        }
        return texture;
    }

    public static <B extends IIdentifiable> DynamicTexture generateTexture(B id, String base64, boolean hasBg) {
        try {
            return setTexture(id, hasBg, new DynamicTexture(NativeImage.fromBase64(base64)));
        } catch (IOException e) {
            ModMain.LOGGER.error("Unable to generate dynamic texture.", e);
            e.printStackTrace();
        }
        return null;
    }
    
    public static <B extends IIdentifiable> DynamicTexture getTexture(B id, String newTexture64, boolean hasBg, Consumer<DynamicTexture> onCreateNewTexture)  {        
        if (newTexture64 == null) {
            return null;
        }

        if (!textureCache.containsKey(id.getId())) {
            DynamicTexture tex = generateTexture(id, newTexture64, hasBg);
            if (tex == null) {
                return null;
            }

            textureCache.put(id.getId(), tex);
            onCreateNewTexture.accept(tex);
        }

        return textureCache.get(id.getId());
    }

    public static <B extends IIdentifiable> DynamicTexture getBackground(B id)  {
        if (!hasBackground(id) || !backgroundTextureCache.containsKey(id.getId())) {
            return null;
        }

        return backgroundTextureCache.get(id.getId());
    }

    public static <B extends IIdentifiable> void clear(B id) {
        if (textureCache.containsKey(id.getId())) {
            textureCache.get(id.getId()).close();
            textureCache.remove(id.getId());
        }

        if (backgroundTextureCache.containsKey(id.getId())) {
            backgroundTextureCache.get(id.getId()).close();
            backgroundTextureCache.remove(id.getId());
        }
    }

    public static void clear(String id) {
        if (textureCache.containsKey(id)) {
            textureCache.get(id).close();
            textureCache.remove(id);
        }

        if (backgroundTextureCache.containsKey(id)) {
            backgroundTextureCache.get(id).close();
            backgroundTextureCache.remove(id);
        }
    }

    public static <B extends IIdentifiable> boolean hasBackground(B id) {
        return backgroundTextureCache.containsKey(id.getId());
    }

    public static <B extends IIdentifiable> String textureToBase64(B id)  {
        return textureToBase64(textureCache.get(id.getId()).getPixels());
    }

    public static String textureToBase64(NativeImage image)  {
        return Utils.textureToBase64(image);
    }

    public static <B extends IIdentifiable> int[][] textureToIntArray(B id, boolean flipRgb) {
        DynamicTexture tex = textureCache.get(id.getId());
        final int[][] a = new int[tex.getPixels().getWidth()][];
        for (int x = 0; x < tex.getPixels().getWidth(); x++) {
            a[x] = new int[tex.getPixels().getHeight()];
            for (int y = 0; y < tex.getPixels().getHeight(); y++) {
                a[x][y] = flipRgb ? Utils.swapRedBlue(tex.getPixels().getPixelRGBA(x, y)) : tex.getPixels().getPixelRGBA(x, y);
            }
        }
        return a;
    }
}

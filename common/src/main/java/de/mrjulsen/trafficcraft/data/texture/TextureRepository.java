package de.mrjulsen.trafficcraft.data.texture;

import de.mrjulsen.trafficcraft.TrafficCraft;
import dev.architectury.utils.GameInstance;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class TextureRepository {

    public static final String BASE_PATH = "data/" + TrafficCraft.MOD_ID + "_textures";
    public static final String FILE_EXTENSION = "nbt";

    private TextureRepository() {
    }

    public static ITexturePayload load(TextureKey key) {
        if (key.isBuiltIn()) {
            return DynamicTextureRegistry.getTextureCodec(key).map(c -> c.decodeBuiltIn(key)).orElseGet(() -> {
                TrafficCraft.LOGGER.error("No codec registered for type: {}", key.getTypeId());
                return ITexturePayload.empty();
            });
        }
        return loadFromDisk(key);
    }

    public static void save(TextureKey key, ITexturePayload payload) {
        if (payload.hasErrors()) return;
        File file = resolveFile(key);
        try {
            file.getParentFile().mkdirs();
            NbtIo.writeCompressed(payload.toNbt(), file);
        } catch (IOException e) {
            TrafficCraft.LOGGER.error("Failed to save texture: {}", key, e);
        }
    }

    private static ITexturePayload loadFromDisk(TextureKey key) {
        File file = resolveFile(key);
        if (!file.exists()) {
            TrafficCraft.LOGGER.error("Texture file not found: {}", key);
            return ITexturePayload.empty();
        }
        try {
            return TexturePayload.fromNbt(NbtIo.readCompressed(file));
        } catch (IOException e) {
            TrafficCraft.LOGGER.error("Failed to load texture: {}", key, e);
            return ITexturePayload.empty();
        }
    }

    private static File resolveFile(TextureKey key) {
        String path = String.format("%s/%s/%s/%s.%s",
                BASE_PATH,
                key.getTypeId().getNamespace(),
                key.getTypeId().getPath(),
                key.getTextureId(),
                FILE_EXTENSION
        );
        LevelResource res = new LevelResource(path);
        return new File(GameInstance.getServer().getWorldPath(res).toString());
    }
}
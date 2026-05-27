package de.mrjulsen.trafficcraft.data.textures;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.textures.data.NbtTextureData;
import dev.architectury.utils.GameInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class TextureRepository {

    public static final String BASE_PATH = "data/custom_textures";
    public static final String NBT_EXT = "nbt";

    private TextureRepository() {
    }

    public static Optional<NbtTextureData> loadCustom(TextureIdentifier id) {
        if (!id.isCustom()) return Optional.empty();
        File file = resolveFile(id);
        if (!file.exists()) {
            TrafficCraft.LOGGER.error("Texture file not found: {}", id);
            return Optional.empty();
        }
        try {
            CompoundTag tag = NbtIo.readCompressed(file);
            return Optional.of(NbtTextureData.fromNbt(tag));
        } catch (IOException e) {
            TrafficCraft.LOGGER.error("Failed to load texture {}: {}", id, e);
            return Optional.empty();
        }
    }

    public static void saveCustom(TextureIdentifier id, NbtTextureData data) {
        File file = resolveFile(id);
        try {
            file.getParentFile().mkdirs();
            NbtIo.writeCompressed(data.toNbt(), file);
        } catch (IOException e) {
            TrafficCraft.LOGGER.error("Failed to save texture {}: {}", id, e);
        }
    }

    private static File resolveFile(TextureIdentifier id) {
        String path = String.format("%s/%s/%s/%s.%s",
                BASE_PATH,
                id.getTypeId().getNamespace(),
                id.getTypeId().getPath(),
                id.getLocation(),
                NBT_EXT
        );
        return new File(GameInstance.getServer().getWorldPath(new LevelResource(path)).toString());
    }
}
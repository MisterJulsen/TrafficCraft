package de.mrjulsen.trafficcraft.data.textures;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import de.mrjulsen.trafficcraft.data.textures.data.ITextureData;
import de.mrjulsen.trafficcraft.data.textures.data.TextureDataType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TextureDataManager extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    public static final TextureDataManager INSTANCE = new TextureDataManager();

    private Map<ResourceLocation, ITextureData> byLocation = Map.of();

    private TextureDataManager() {
        super(GSON, "custom_textures");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, ITextureData> locations = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation key = entry.getKey();
            try {
                ITextureData data = TextureDataCodec.CODEC
                        .parse(JsonOps.INSTANCE, entry.getValue())
                        .getOrThrow(false, e -> LOGGER.error("Failed to parse {}: {}", key, e));

                locations.put(key, data);
            } catch (Exception e) {
                LOGGER.error("Skipping texture definition {}: {}", key, e);
            }
        }

        this.byLocation = Map.copyOf(locations);
        LOGGER.info("Loaded {} texture definitions.", byLocation.size());
    }

    public Optional<ITextureData> get(ResourceLocation key) {
        return Optional.ofNullable(byLocation.get(key));
    }

    public Collection<ResourceLocation> getAll() {
        return Collections.unmodifiableCollection(byLocation.keySet());
    }

    public <T extends ITextureData> List<ResourceLocation> getByType(TextureDataType<T> type, Predicate<T> filter) {
        return byLocation.entrySet().stream()
                .filter(e -> e.getValue().getType().equals(type) && filter.test((T) e.getValue()))
                .map(Map.Entry::getKey).toList();
    }

    public <T extends ITextureData, K> Map<K, List<ResourceLocation>> getByTypeGrouped(TextureDataType<T> type, Predicate<T> filter, Function<T, K> keyMapper) {
        return byLocation.entrySet().stream()
                .filter(e -> e.getValue().getType().equals(type) && filter.test((T) e.getValue()))
                .collect(Collectors.groupingBy(
                        e -> keyMapper.apply((T) e.getValue()),
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));
    }
}
package de.mrjulsen.trafficcraft.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.blaze3d.platform.NativeImage;

import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.Pair.MutablePair;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference.BuildInTrafficSignCodec;
import de.mrjulsen.trafficcraft.network.packets.cts.CreateNewTrafficSignTexturePacket;
import de.mrjulsen.trafficcraft.network.packets.cts.GetTrafficSignTexturePacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import dev.architectury.utils.GameInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class TrafficSignClientTexture implements AutoCloseable {

    public static final DynamicTexture EMPTY_TEXTURE;
    public static final TrafficSignClientTexture EMPTY;
    public static final ResourceLocation EMPTY_LOCATION;

    static {
        NativeImage img = new NativeImage(1, 1, false);
        img.setPixelRGBA(0, 0, 0x00000000);
        EMPTY_TEXTURE = new DynamicTexture(img);
        EMPTY_LOCATION = new ResourceLocation(TrafficCraft.MOD_ID, "empty_sign");
        Minecraft.getInstance().getTextureManager().register(EMPTY_LOCATION, EMPTY_TEXTURE);
        EMPTY = new TrafficSignClientTexture("empty");
    }

    public static final Map<String, MutablePair<TrafficSignClientTexture, Integer>> cachedTexturesById = new HashMap<>();

    public static int debug_cachedTexturesCount() { return cachedTexturesById.size(); }

    public static int closeAll() {
        int count = cachedTexturesById.size();
        new ArrayList<>(cachedTexturesById.values()).stream().map(x -> x.getFirst()).forEach(x -> x.close());
        cachedTexturesById.clear();
        return count;
    }

    protected TrafficSignTextureData rawData = TrafficSignTextureData.empty();
    protected DynamicTexture texture = EMPTY_TEXTURE;
    protected DynamicTexture backgroundTexture = EMPTY_TEXTURE;
    protected ResourceLocation textureLocation = EMPTY_LOCATION;
    protected ResourceLocation backgroundTextureLocation = EMPTY_LOCATION;
    protected final String textureId;

    private boolean isClosed = false;
    private boolean builtIn = false;

    protected TrafficSignClientTexture(String textureId) {
        this.textureId = textureId;
    }

    private synchronized TrafficSignClientTexture init(TrafficSignTextureData rawData, boolean createBg) {
        if (isClosed) return this;

        this.rawData = rawData;
        DynamicTexture tex = EMPTY_TEXTURE;
        if (rawData.getPixelData().length > 0) {
            try {
                tex = new DynamicTexture(NativeImage.read(new ByteArrayInputStream(rawData.getPixelData())));
            } catch (IOException e) {
                TrafficCraft.LOGGER.error("Unable to load texture.", e);
                tex = MissingTextureAtlasSprite.getTexture();
            }
        }
        this.texture = tex;
        this.textureLocation = new ResourceLocation(TrafficCraft.MOD_ID, "sign_" + rawData.getHash().toString());

        if (createBg && rawData.getShape() == TrafficSignShape.MISC) {
            generateBgTexture();
        }

        Minecraft.getInstance().getTextureManager().register(textureLocation, tex);
        return this;
    }

    private void generateBgTexture() {
        DynamicTexture originalTexture = EMPTY_TEXTURE;
        try {
            String idSuffix = "_";
            if (isBuiltIn()) {
                BuildInTrafficSignCodec codec = BuildInTrafficSignCodec.decode(textureId);
                idSuffix = "" + codec.id() + "_bg";
                originalTexture = new DynamicTexture(NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(getTextureLocation()).get().open()));
            } else {
                idSuffix = "_bg";
                originalTexture = texture;
            }

            if (originalTexture == null || originalTexture == EMPTY_TEXTURE) {
                return;
            }
            NativeImage bg = NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(TrafficCraft.MOD_ID, "textures/block/sign/blank.png")).get().open());            
            final int width = Math.min(bg.getWidth(), TrafficSignShape.MAX_WIDTH);
            final int height = Math.min(bg.getHeight(), TrafficSignShape.MAX_HEIGHT);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (originalTexture.getPixels().getPixelRGBA(x, y) != 0)
                        continue;

                    bg.setPixelRGBA(width - 1 - x, y, 0);
                }
            }
            
            this.backgroundTexture = new DynamicTexture(bg);
            this.backgroundTextureLocation = new ResourceLocation(TrafficCraft.MOD_ID, "sign_" + (isBuiltIn() ? rawData.getShape().getIndex() : rawData.getHash().toString()) + idSuffix);
            Minecraft.getInstance().getTextureManager().register(backgroundTextureLocation, backgroundTexture);
        } catch (Exception e) {
            TrafficCraft.LOGGER.error("Unable to create traffic sign background texture.", e);
        } finally {
            if (isBuiltIn() && originalTexture != null && originalTexture != EMPTY_TEXTURE) {            
                originalTexture.close();
            }
        }
    }

    public static TrafficSignTextureData createNew(TrafficSignShape shape, NativeImage image, Runnable andThen) {
        TrafficSignTextureData data;
        try {
            data = new TrafficSignTextureData(
                shape,
                image.asByteArray(),
                (short)image.getWidth(),
                (short)image.getHeight(),
                System.currentTimeMillis(),
                GameInstance.getClient().player.getUUID()
            );
            ModNetworkManager.CREATE_NEW_TRAFFIC_SIGN_TEXTURE.send(NetworkDirection.toServer(), new CreateNewTrafficSignTexturePacket.Request(data), (response) -> {
                DLUtils.doIfNotNull(andThen, x -> x.run());
            }, () -> {});
        } catch (IOException e) {
            TrafficCraft.LOGGER.error("Unable to create new traffic sign texture.", e);
            data = TrafficSignTextureData.empty();
        }
        return data;
    }

    public static TrafficSignClientTexture load(String id, boolean allowBackground, Runnable afterLoad) {
        MutablePair<TrafficSignClientTexture, Integer> data = cachedTexturesById.computeIfAbsent(id, x -> {            
            TrafficSignClientTexture textureData = new TrafficSignClientTexture(id);
            if (id.startsWith(BuildInTrafficSignCodec.PREFIX)) {
                try {
                    BuildInTrafficSignCodec codec = BuildInTrafficSignCodec.decode(id);
                    textureData.builtIn = true;
                    textureData.texture = EMPTY_TEXTURE;
                    textureData.textureLocation = new ResourceLocation(TrafficCraft.MOD_ID, String.format("textures/block/sign/%s/%s%s.png", codec.shape().getSerializedName(), codec.shape().getSerializedName(), codec.id()));
                    textureData.rawData = new TrafficSignTextureData(codec.shape(), new byte[0], codec.width(), codec.height(), System.currentTimeMillis(), new UUID(0, 0));
                } catch (Exception e) {
                    TrafficCraft.LOGGER.error("Error while loading TrafficSignClientTexture.", e);
                }
            } else {
                ModNetworkManager.GET_TRAFFIC_SIGN_TEXTURE.send(NetworkDirection.toServer(), new GetTrafficSignTexturePacket.Request(id), (response) -> {
                    textureData.init(response.getData(), allowBackground);
                    DLUtils.doIfNotNull(afterLoad, Runnable::run);
                }, () -> {});
            }
            return new MutablePair<>(textureData, 0);
        });
        data.setSecond(data.getSecond() + 1);
        if (allowBackground && data.getFirst().getRawData().getShape() == TrafficSignShape.MISC && data.getFirst().isFullyLoaded()) {
            
            data.getFirst().generateBgTexture();
        }
        return data.getFirst();
    }

    public static void unload(UUID id) {
        unload(id.toString());
    }

    protected static void unload(String id) {
        if (!cachedTexturesById.containsKey(id)) {
            TrafficCraft.LOGGER.warn("There was no cached traffic sign texture with id " + id + ".");
            return;
        }
        MutablePair<TrafficSignClientTexture, Integer> data = cachedTexturesById.get(id);
        data.setSecond(data.getSecond() - 1);

        if (data.getSecond() <= 0) {
            cachedTexturesById.remove(id).getFirst().closeInternal();
        }
    }

    public TrafficSignTextureData getRawData() {
        return rawData;
    }

    public DynamicTexture getTexture() {
        if (builtIn) {
            throw new IllegalAccessError("Cannot access built-in textures as DynamicTexture.");
        }
        return texture;
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public DynamicTexture getBackgroundTexture() {
        if (builtIn) {
            throw new IllegalAccessError("Cannot access built-in textures as DynamicTexture.");
        }
        return backgroundTexture;
    }

    public ResourceLocation getBackgroundTextureLocation() {
        return backgroundTextureLocation;
    }

    public boolean isFullyLoaded() {
        return !this.equals(EMPTY);
    }

    public boolean isDisposed() {
        return isClosed;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public boolean hasBackground() {
        return backgroundTexture != null && backgroundTexture != EMPTY_TEXTURE;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrafficSignClientTexture o) {
            return textureLocation.equals(o.textureLocation);
        }
        return super.equals(obj);
    }

    @Override
    public void close() {
        unload(textureId);
    }

    private void closeInternal() {
        isClosed = true;
        if (this != EMPTY && !this.equals(EMPTY) && !this.isBuiltIn()) {
            DLUtils.doIfNotNull(texture, x -> x.close());
            Minecraft.getInstance().getTextureManager().release(textureLocation);

            if (backgroundTexture != null && backgroundTexture != EMPTY_TEXTURE) {
                backgroundTexture.close();
                Minecraft.getInstance().getTextureManager().release(backgroundTextureLocation);
            }
        }
    }
}

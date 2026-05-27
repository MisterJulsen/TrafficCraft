package de.mrjulsen.trafficcraft.data.textures.data;

import de.mrjulsen.mcdragonlib.util.NbtUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.textures.TextureDataCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

import java.util.Map;
import java.util.UUID;

public class NbtTextureData implements ITextureData {

    private final ITextureData inner;
    private final byte[] rawBytes;
    private final int width, height;
    private final Map<String, CompoundTag> metadata;

    private UUID owner;
    private long createdAt;


    private static final String NBT_INNER_DATA = "Data";
    private static final String NBT_TEXTURE = "Texture";
    private static final String NBT_WIDTH = "Width";
    private static final String NBT_HEIGHT = "Height";
    private static final String NBT_OWNER = "Owner";
    private static final String NBT_CREATED = "Created";
    private static final String NBT_META = "Metadata";

    public NbtTextureData(ITextureData inner, byte[] rawBytes, int width, int height, UUID owner, long createdAt, Map<String, CompoundTag> metadata) {
        this.inner = inner;
        this.rawBytes = rawBytes;
        this.width = width;
        this.height = height;
        this.owner = owner;
        this.createdAt = createdAt;
        this.metadata = metadata;
    }

    @Override
    public TextureDataType<? extends ITextureData> getType() {
        return inner.getType();
    }

    @Override
    public TextureSource getSource() {
        return new TextureSource.Custom(rawBytes, width, height);
    }

    public byte[] getRawBytes() {
        return rawBytes;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public UUID getOwner() {
        return owner;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public ITextureData getInner() {
        return inner;
    }

    public Map<String, CompoundTag> getCustomMetadata() {
        return metadata;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        TextureDataCodec.CODEC.encodeStart(NbtOps.INSTANCE, inner)
                .resultOrPartial(e -> TrafficCraft.LOGGER.error("NBT encode error: {}", e))
                .ifPresent(t -> tag.put(NBT_INNER_DATA, t));
        tag.putByteArray(NBT_TEXTURE, rawBytes);
        tag.putInt(NBT_WIDTH, width);
        tag.putInt(NBT_HEIGHT, height);
        tag.putUUID(NBT_OWNER, owner);
        tag.putLong(NBT_CREATED, createdAt);
        NbtUtils.putMap(tag, NBT_META, metadata, k -> k, v -> v);
        return tag;
    }

    public static NbtTextureData fromNbt(CompoundTag tag) {
        ITextureData inner = TextureDataCodec.CODEC
                .parse(NbtOps.INSTANCE, tag.getCompound(NBT_INNER_DATA))
                .resultOrPartial(e -> {
                    TrafficCraft.LOGGER.error("NBT decode error: {}", e);
                })
                .orElseThrow();
        byte[] bytes = tag.getByteArray(NBT_TEXTURE);
        int width = tag.getInt(NBT_WIDTH);
        int height = tag.getInt(NBT_HEIGHT);
        UUID owner = tag.getUUID(NBT_OWNER);
        long created = tag.getLong(NBT_CREATED);
        Map<String, CompoundTag> metadata = NbtUtils.getMap(tag, NBT_META, k -> k, v -> v);
        return new NbtTextureData(inner, bytes, width, height, owner, created, metadata);
    }
}
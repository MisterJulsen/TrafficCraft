package de.mrjulsen.trafficcraft.data.texture;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.registry.ModClientRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.UUID;

public final class TexturePayload implements ITexturePayload {

    private static final String NBT_VERSION = "Version";
    private static final String NBT_PAYLOAD_TYPE = "PayloadType";
    private static final String NBT_HASH = "Hash";
    private static final String NBT_DATA = "Data";
    private static final String NBT_WIDTH = "Width";
    private static final String NBT_HEIGHT = "Height";
    private static final String NBT_CREATION_TIME = "CreationTime";
    private static final String NBT_OWNER = "Owner";
    private static final String NBT_USES = "Uses";
    private static final String NBT_LAST_ACCESS = "LastAccessTime";
    private static final String NBT_CUSTOM_DATA = "CustomData";

    public static final int VERSION = 1;

    private final int version;
    private final IPayloadDecoder decoder;
    private final byte[] data;
    private final short width;
    private final short height;
    private final long creationTime;
    private final UUID owner;
    private final UUID hash;
    private final CompoundTag customData;

    private int uses;
    private long lastAccessTime;
    private boolean hasErrors;

    public TexturePayload(IPayloadDecoder type, byte[] data, short width, short height, long creationTime, UUID owner, CompoundTag customData) {
        this(VERSION, type, data, width, height, creationTime, owner, customData);
    }

    private TexturePayload(int version, IPayloadDecoder type, byte[] data, short width, short height, long creationTime, UUID owner, CompoundTag customData) {
        this.version = version;
        this.decoder = type;
        this.data = data;
        this.width = width;
        this.height = height;
        this.creationTime = creationTime;
        this.owner = owner;
        this.customData = customData != null ? customData : new CompoundTag();
        this.hash = calculateHash();
    }

    public static TexturePayload empty() {
        return new TexturePayload(
                ModClientRegistries.RGBA_TEXTURE.decoder(), new byte[0],
                (short) 1, (short) 1,
                System.currentTimeMillis(), new UUID(0, 0), null
        );
    }

    private UUID calculateHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA3-512");
            md.update(decoder.getDecoderId().toString().getBytes());
            byte[] raw = md.digest(data);
            ByteBuffer buf = ByteBuffer.wrap(raw);
            return new UUID(buf.getLong(), buf.getLong());
        } catch (Exception e) {
            TrafficCraft.LOGGER.error("Unable to calculate texture hash.", e);
            return new UUID(0, 0);
        }
    }

    @Override
    public IPayloadDecoder getDecoder() {
        return decoder;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public short getWidth() {
        return width;
    }

    @Override
    public short getHeight() {
        return height;
    }

    @Override
    public UUID getHash() {
        return hash;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public boolean hasErrors() {
        return hasErrors;
    }

    @Override
    public CompoundTag getCustomData() {
        return customData;
    }

    public int getUses() {
        return uses;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long t) {
        this.lastAccessTime = t;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(NBT_VERSION, VERSION);
        nbt.putString(NBT_PAYLOAD_TYPE, decoder.getDecoderId().toString());
        nbt.putUUID(NBT_HASH, hash);
        nbt.putByteArray(NBT_DATA, data);
        nbt.putShort(NBT_WIDTH, width);
        nbt.putShort(NBT_HEIGHT, height);
        nbt.putLong(NBT_CREATION_TIME, creationTime);
        nbt.putUUID(NBT_OWNER, owner);
        nbt.putInt(NBT_USES, uses);
        nbt.putLong(NBT_LAST_ACCESS, lastAccessTime);
        nbt.put(NBT_CUSTOM_DATA, customData);
        return nbt;
    }

    public static TexturePayload fromNbt(CompoundTag nbt) {
        TexturePayload p = DynamicTextureRegistry.getTextureDecoder(new ResourceLocation(nbt.getString(NBT_PAYLOAD_TYPE))).map(d ->
            new TexturePayload(
                    nbt.getInt(NBT_VERSION),
                    d,
                    nbt.getByteArray(NBT_DATA),
                    nbt.getShort(NBT_WIDTH),
                    nbt.getShort(NBT_HEIGHT),
                    nbt.getLong(NBT_CREATION_TIME),
                    nbt.getUUID(NBT_OWNER),
                    nbt.getCompound(NBT_CUSTOM_DATA)
            )
        ).orElse(TexturePayload.empty());
        p.uses = nbt.getInt(NBT_USES);
        p.lastAccessTime = nbt.getLong(NBT_LAST_ACCESS);

        UUID stored = nbt.getUUID(NBT_HASH);
        UUID computed = p.getHash();
        if (!stored.equals(computed)) {
            TrafficCraft.LOGGER.warn("Hash mismatch! stored={} computed={}", stored, computed);
            p.hasErrors = true;
        }
        return p;
    }
}
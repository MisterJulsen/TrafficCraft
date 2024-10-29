package de.mrjulsen.trafficcraft.data;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import dev.architectury.utils.GameInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

public class TrafficSignTextureData {

    private static final String NBT_VERSION = "Version";
    private static final String NBT_HASH = "Hash";
    private static final String NBT_PIXEL_DATA = "Data";
    private static final String NBT_WIDTH = "Width";
    private static final String NBT_HEIGHT = "Height";
    private static final String NBT_CREATION_TIME = "CreationTime";
    private static final String NBT_OWNER = "Owner";
    private static final String NBT_USES = "Uses";
    private static final String NBT_LAST_ACCESS_TIME = "LastAccessTime";
    private static final String NBT_SHAPE = "Shape";

    public static final int VERSION = 1;

    private final int version;
    private final UUID hash;
    private final byte[] pixelData;
    private final short width;
    private final short height;
    private final long creationTime;
    private final UUID owner;
    private final TrafficSignShape shape;

    private int uses;
    private long lastAccessTime;

    private boolean hasErrors = false;
    
    
    public TrafficSignTextureData(TrafficSignShape shape, byte[] pixelData, short width, short height, long creationTime, UUID owner) {
        this(VERSION, shape, pixelData, width, height, creationTime, owner);
    }

    private TrafficSignTextureData(int version, TrafficSignShape shape, byte[] pixelData, short width, short height, long creationTime, UUID owner) {
        this.version = version;
        this.shape = shape;
        this.pixelData = pixelData;
        this.width = width;
        this.height = height;
        this.creationTime = creationTime;
        this.owner = owner;

        this.hash = calculateHash();
    }

    public static TrafficSignTextureData empty() {
        return new TrafficSignTextureData(TrafficSignShape.SQUARE, new byte[0], (short)1, (short)1, System.currentTimeMillis(), new UUID(0, 0));
    }

    public UUID calculateHash() {
        try {
            MessageDigest md = MessageDigest.getInstance(MessageDigestAlgorithms.SHA3_512);
            byte[] hash = md.digest(pixelData);
            ByteBuffer byteBuffer = ByteBuffer.wrap(hash);
            long high = byteBuffer.getLong();
            long low = byteBuffer.getLong();
            
            UUID id = new UUID(high, low);
            return id;
        } catch (NoSuchAlgorithmException e) {
            TrafficCraft.LOGGER.error("Unable to calculate texture hash value.", e);
        }
        return new UUID(0, 0);
    }

    public int getVersion() {
        return version;
    }

    public UUID getHash() {
        return hash;
    }

    public byte[] getPixelData() {
        return pixelData;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public UUID getOwner() {
        return owner;
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

    public void setLastAccessTime(long lastLoadedTime) {
        this.lastAccessTime = lastLoadedTime;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    public TrafficSignShape getShape() {
        return shape;
    }

    public void save() {
        if (hasErrors()) {
            return;
        }

        try {
            File file = new File(GameInstance.getServer().getWorldPath(TrafficSignTextureManager.getResource(getHash().toString())).toString());
            file.getParentFile().mkdirs();
            NbtIo.writeCompressed(serializeNbt(), file);
        } catch (IOException e) {
            TrafficCraft.LOGGER.error("Unable to save traffic sign texture file.", e);
        }
    }

    public CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(NBT_VERSION, VERSION);
        nbt.putUUID(NBT_HASH, hash);
        nbt.putInt(NBT_SHAPE, shape.getIndex());
        nbt.putByteArray(NBT_PIXEL_DATA, pixelData);
        nbt.putShort(NBT_WIDTH, width);
        nbt.putShort(NBT_HEIGHT, height);
        nbt.putLong(NBT_CREATION_TIME, creationTime);
        nbt.putUUID(NBT_OWNER, owner);
        nbt.putInt(NBT_USES, uses);
        nbt.putLong(NBT_LAST_ACCESS_TIME, lastAccessTime);
        return nbt;
    }

    public static TrafficSignTextureData deserializeNbt(CompoundTag nbt) {
        TrafficSignTextureData data = new TrafficSignTextureData(
            nbt.getInt(NBT_VERSION),
            TrafficSignShape.getShapeByIndex(nbt.getInt(NBT_SHAPE)),
            nbt.getByteArray(NBT_PIXEL_DATA),
            nbt.getShort(NBT_WIDTH), 
            nbt.getShort(NBT_HEIGHT), 
            nbt.getLong(NBT_CREATION_TIME), 
            nbt.getUUID(NBT_OWNER)
        );
        data.uses = nbt.getInt(NBT_USES);
        data.lastAccessTime = nbt.getLong(NBT_LAST_ACCESS_TIME);

        UUID newHash = data.getHash();
        UUID oldHash = nbt.getUUID(NBT_HASH);

        if (!newHash.equals(oldHash)) {
            TrafficCraft.LOGGER.warn("The Hash value of this traffic sign texture has been changed! Is " + newHash + ", was " + oldHash + ".");
            data.hasErrors = true;
        }

        return data;
    }

    
}

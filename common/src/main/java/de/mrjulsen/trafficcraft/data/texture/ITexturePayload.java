package de.mrjulsen.trafficcraft.data.texture;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public interface ITexturePayload {
    IPayloadDecoder getDecoder();
    byte[] getData();
    short getWidth();
    short getHeight();
    UUID getHash();
    long getCreationTime();
    UUID getOwner();
    boolean hasErrors();
    CompoundTag getCustomData();

    CompoundTag toNbt();

    static ITexturePayload empty() {
        return TexturePayload.empty();
    }
}
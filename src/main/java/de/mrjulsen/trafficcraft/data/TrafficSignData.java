package de.mrjulsen.trafficcraft.data;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public class TrafficSignData {

    private final int width;
    private final int height;
    private NativeImage texture;
    private String name;    

    public TrafficSignData(int width, int height) {
        this.width = width;
        this.height = height;
        this.texture = new NativeImage(Format.RGBA, width, height, false);        
    }

    public NativeImage getTexture() {
        return texture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public NativeImage setFromBase64(String base64) {
        try {
            return this.texture = NativeImage.fromBase64(base64);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getPixelRGBA(int x, int y) {
        return texture.getPixelRGBA(Mth.clamp(x, 0, width), Mth.clamp(y, 0, height));
    }

    public void setPixelRGBA(int x, int y, int rgba) {
        texture.setPixelRGBA(Mth.clamp(x, 0, width), Mth.clamp(y, 0, height), rgba);
    }

    private String textureToBase64() {
        try {
            return Base64.encodeBase64String(this.getTexture().asByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


    /* DATA STORAGE */

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("width", width);
        tag.putInt("height", height);
        tag.putString("name", name);
        tag.putString("pixelData", textureToBase64());
        return tag;
    }

    public static TrafficSignData fromNbt(CompoundTag tag) {
        TrafficSignData data = new TrafficSignData(tag.getInt("width"), tag.getInt("height"));
        data.setName(tag.getString("name"));
        data.setFromBase64(tag.getString("pixelData"));
        return data;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(width);
        buf.writeInt(height);
        buf.writeUtf(name);
        buf.writeUtf(textureToBase64());
    }

    public static TrafficSignData fromBytes(FriendlyByteBuf buf) {
        TrafficSignData data = new TrafficSignData(buf.readInt(), buf.readInt());
        data.setName(buf.readUtf());
        data.setFromBase64(buf.readUtf());
        return data;
    }
}

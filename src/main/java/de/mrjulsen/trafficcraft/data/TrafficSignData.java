package de.mrjulsen.trafficcraft.data;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.block.properties.TrafficSignShape;
import de.mrjulsen.trafficcraft.util.Utils;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

public class TrafficSignData implements AutoCloseable {

    private final int width;
    private final int height;
    private NativeImage texture;
    private DynamicTexture dynTex;
    private final TrafficSignShape shape;
    private String name = "";    

    public TrafficSignData(int width, int height, TrafficSignShape shape) {
        this.width = width;
        this.height = height;
        this.shape = shape;
        this.texture = new NativeImage(Format.RGBA, width, height, false);
        this.clearImage();

        NativeImage copy = new NativeImage(width, height, false);
        copy.copyFrom(texture);
        this.dynTex = new DynamicTexture(copy);
    }

    public NativeImage getTexture() {
        return texture;
    }

    public DynamicTexture getDynamicTexture() {
        return dynTex;
    }

    public TrafficSignShape getShape() {
        return shape;
    }

    public String getName() {
        return name == null || name.isEmpty() ? new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.pattern.name_unknown").getString(): name;
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

    public void clearImage() {
        texture.fillRect(0, 0, width, height, 0);

        if (dynTex != null) {
            NativeImage copy = new NativeImage(width, height, false);
            copy.copyFrom(texture);
            this.dynTex.setPixels(copy);
            this.dynTex.upload();
        }
    }

    public NativeImage setFromBase64(String base64) {
        try {
            this.texture = NativeImage.fromBase64(base64); 

            NativeImage copy = new NativeImage(width, height, false);
            copy.copyFrom(texture);
            this.dynTex.setPixels(copy);
            this.dynTex.upload();
            return this.texture;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int[][] textureToIntArray(boolean flipRgb) {
        final int[][] a = new int[width][];
        for (int x = 0; x < width; x++) {
            a[x] = new int[height];
            for (int y = 0; y < height; y++) {
                a[x][y] = flipRgb ? Utils.swapRedBlue(this.getTexture().getPixelRGBA(x, y)) : this.getTexture().getPixelRGBA(x, y);
            }
        }
        return a;
    }

    public void setImage(NativeImage img) {
        this.texture = new NativeImage(Format.RGBA, width, height, false);
        this.clearImage();
        this.texture.copyFrom(img);           
        
        NativeImage copy = new NativeImage(width, height, false);
        copy.copyFrom(texture);
        this.dynTex.setPixels(copy);
        this.dynTex.upload();
    }

    public int getPixelRGBA(int x, int y) {
        return texture.getPixelRGBA(Mth.clamp(x, 0, width), Mth.clamp(y, 0, height));
    }

    public void setPixelRGBA(int x, int y, int rgba) {
        if (!shape.isPixelValid(x, y))
            return;

        texture.setPixelRGBA(Mth.clamp(x, 0, width), Mth.clamp(y, 0, height), rgba);                
        
        NativeImage copy = new NativeImage(width, height, false);
        copy.copyFrom(texture);
        this.dynTex.setPixels(copy);
        this.dynTex.upload();
    }

    private String textureToBase64() {
        try {
            return Base64.encodeBase64String(this.getTexture().asByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void render(PoseStack stack, int x, int y, int w, int h) {
        RenderSystem.setShaderTexture(0, shape.getShapeTextureId());
        RenderSystem.setShaderColor(0, 0, 0, 1);
        GuiComponent.blit(stack, x - 1, y - 1, w, h, 0, 0, 32, 32, 32, 32);
        GuiComponent.blit(stack, x + 1, y - 1, w, h, 0, 0, 32, 32, 32, 32);
        GuiComponent.blit(stack, x - 1, y + 1, w, h, 0, 0, 32, 32, 32, 32);
        GuiComponent.blit(stack, x + 1, y + 1, w, h, 0, 0, 32, 32, 32, 32);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        GuiComponent.blit(stack, x, y, w, h, 0, 0, 32, 32, 32, 32);

        RenderSystem.setShaderTexture(0, dynTex.getId());
        GuiComponent.blit(stack, x, y, w, h, 0, 0, width, height, width, height);
    }


    /* DATA STORAGE */

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("width", width);
        tag.putInt("height", height);
        tag.putInt("shape", shape.getIndex());
        tag.putString("name", name);
        tag.putString("pixelData", textureToBase64());
        return tag;
    }

    public static TrafficSignData fromNbt(CompoundTag tag) {
        TrafficSignData data = new TrafficSignData(tag.getInt("width"), tag.getInt("height"), TrafficSignShape.getShapeByIndex(tag.getInt("shape")));
        data.setName(tag.getString("name"));
        data.setFromBase64(tag.getString("pixelData"));
        return data;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(width);
        buf.writeInt(height);
        buf.writeInt(shape.getIndex());
        buf.writeUtf(name);
        buf.writeUtf(textureToBase64());
    }

    public static TrafficSignData fromBytes(FriendlyByteBuf buf) {
        TrafficSignData data = new TrafficSignData(buf.readInt(), buf.readInt(), TrafficSignShape.getShapeByIndex(buf.readInt()));
        data.setName(buf.readUtf());
        data.setFromBase64(buf.readUtf());
        return data;
    }

    @Override
    public void close() {
        texture.close();
        dynTex.close();
    }
}

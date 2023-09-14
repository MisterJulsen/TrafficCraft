package de.mrjulsen.trafficcraft.data;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

public class TrafficSignData implements Closeable {

    private final int width;
    private final int height;
    private DynamicTexture dynTex;
    private final TrafficSignShape shape;
    private String name = "";    

    public TrafficSignData(int width, int height, TrafficSignShape shape) {
        this.width = width;
        this.height = height;
        this.shape = shape;
        NativeImage texture = new NativeImage(Format.RGBA, width, height, false);
        this.clearImage(texture);
        this.dynTex = new DynamicTexture(texture);
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

    public void clearImage(NativeImage texture) {
        texture.fillRect(0, 0, width, height, 0);
    }

    public void setFromBase64(String base64) {
        try {
            NativeImage texture = NativeImage.fromBase64(base64); 
            this.dynTex.setPixels(texture);
            this.dynTex.upload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int[][] textureToIntArray(boolean flipRgb) {
        final int[][] a = new int[width][];
        for (int x = 0; x < width; x++) {
            a[x] = new int[height];
            for (int y = 0; y < height; y++) {
                a[x][y] = flipRgb ? Utils.swapRedBlue(this.getDynamicTexture().getPixels().getPixelRGBA(x, y)) : this.getDynamicTexture().getPixels().getPixelRGBA(x, y);
            }
        }
        return a;
    }

    public void setImage(NativeImage img) {
        this.dynTex.setPixels(img);
        this.dynTex.upload();
    }

    public int getPixelRGBA(int x, int y) {
        return this.getDynamicTexture().getPixels().getPixelRGBA(Mth.clamp(x, 0, width), Mth.clamp(y, 0, height));
    }

    public void setPixelRGBA(int x, int y, int rgba) {
        if (!shape.isPixelValid(x, y))
            return;

        NativeImage texture = this.getDynamicTexture().getPixels();
        texture.setPixelRGBA(Mth.clamp(x, 0, width), Mth.clamp(y, 0, height), rgba); 
        this.dynTex.upload();
    }

    private String textureToBase64() {
        try {
            return Base64.encodeBase64String(this.getDynamicTexture().getPixels().asByteArray());
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

        String texture = textureToBase64();
        int length = texture.getBytes(StandardCharsets.UTF_8).length;
        buf.writeInt(length);
        buf.writeUtf(texture, length);
    }

    public static TrafficSignData fromBytes(FriendlyByteBuf buf) {
        TrafficSignData data = new TrafficSignData(buf.readInt(), buf.readInt(), TrafficSignShape.getShapeByIndex(buf.readInt()));
        data.setName(buf.readUtf());

        int length = buf.readInt();
        data.setFromBase64(buf.readUtf(length));
        return data;
    }

    @Override
    public void close() {
        if (dynTex != null) {
            dynTex.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.clone();
    }
}

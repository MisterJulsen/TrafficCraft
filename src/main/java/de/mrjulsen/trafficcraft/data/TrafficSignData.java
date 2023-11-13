package de.mrjulsen.trafficcraft.data;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.block.entity.IIdentifiable;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.client.TrafficSignTextureCacheClient;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class TrafficSignData implements Closeable, IIdentifiable {

    private final String ID;

    private final int width;
    private final int height;

    private String texture;

    private final TrafficSignShape shape;
    private String name = "";    

    public TrafficSignData(int width, int height, TrafficSignShape shape) {
        this.width = width;
        this.height = height;
        this.shape = shape;

        ID = String.valueOf(System.nanoTime());
    }

    @Override
    public String getId() {
        return ID;
    }

    public String getTexture() {
        return texture;
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
        texture = base64;
    }

    @OnlyIn(Dist.CLIENT)
    public void setPixelRGBA(int x, int y, int rgba) {
        if (!shape.isPixelValid(x, y))
            return;

        DynamicTexture tex = TrafficSignTextureCacheClient.getTexture(this, texture, false, (texture) -> {
            this.texture = TrafficSignTextureCacheClient.textureToBase64(this);
        });
        
        NativeImage texture = tex.getPixels();
        texture.setPixelRGBA(Mth.clamp(x, 0, width), Mth.clamp(y, 0, height), rgba); 
        tex.upload();
    }

    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack stack, int x, int y, int w, int h) {
        DynamicTexture tex = TrafficSignTextureCacheClient.getTexture(this, texture, false, (texture) -> {
            this.texture = TrafficSignTextureCacheClient.textureToBase64(this);
        });

        RenderSystem.setShaderTexture(0, shape.getShapeTextureId());
        RenderSystem.setShaderColor(0, 0, 0, 1);
        GuiComponent.blit(stack, x - 1, y - 1, w, h, 0, 0, 32, 32, 32, 32);
        GuiComponent.blit(stack, x + 1, y - 1, w, h, 0, 0, 32, 32, 32, 32);
        GuiComponent.blit(stack, x - 1, y + 1, w, h, 0, 0, 32, 32, 32, 32);
        GuiComponent.blit(stack, x + 1, y + 1, w, h, 0, 0, 32, 32, 32, 32);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        GuiComponent.blit(stack, x, y, w, h, 0, 0, 32, 32, 32, 32);

        RenderSystem.setShaderTexture(0, tex.getId());
        GuiComponent.blit(stack, x, y, w, h, 0, 0, width, height, width, height);
    }


    /* DATA STORAGE */

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("width", width);
        tag.putInt("height", height);
        tag.putInt("shape", shape.getIndex());
        tag.putString("name", name);
        tag.putString("pixelData", texture);  
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
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.clearTexture(this));
    }

    @Override
    protected void finalize() {
        this.close();
    }
}

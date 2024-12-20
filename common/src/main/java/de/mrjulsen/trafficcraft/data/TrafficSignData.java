package de.mrjulsen.trafficcraft.data;

import java.io.Closeable;
import java.util.Base64;
import java.util.UUID;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.mcdragonlib.core.IIdentifiable;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.accessor.DataAccessor;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.registry.ModAccessorTypes;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;

@Deprecated
public class TrafficSignData implements Closeable, IIdentifiable {

    private static final String NBT_WIDTH = "width";
    private static final String NBT_HEIGHT = "height";
    private static final String NBT_SHAPE = "shape";
    private static final String NBT_NAME = "name";
    private static final String NBT_PIXEL_DATA = "pixelData";

    private final String ID;

    private final int width;
    private final int height;
    private final TrafficSignShape shape;
    private String name = ""; 

    private String texture;
  

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
        return name == null || name.isEmpty() ? TextUtils.translate("gui.trafficcraft.trafficsignworkbench.pattern.name_unknown").getString(): name;
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

    /*
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

    public void render(Graphics graphics, int x, int y, int w, int h) {
        DynamicTexture tex = TrafficSignTextureCacheClient.getTexture(this, texture, false, (texture) -> {
            this.texture = TrafficSignTextureCacheClient.textureToBase64(this);
        });

        GuiUtils.setTint(0, 0, 0, 1);
        GuiUtils.drawTexture(shape.getShapeTextureId(), graphics, x - 1, y - 1, w, h, 0, 0, 32, 32, 32, 32);
        GuiUtils.drawTexture(shape.getShapeTextureId(), graphics, x + 1, y - 1, w, h, 0, 0, 32, 32, 32, 32);
        GuiUtils.drawTexture(shape.getShapeTextureId(), graphics, x - 1, y + 1, w, h, 0, 0, 32, 32, 32, 32);
        GuiUtils.drawTexture(shape.getShapeTextureId(), graphics, x + 1, y + 1, w, h, 0, 0, 32, 32, 32, 32);
        GuiUtils.resetTint();
        GuiUtils.drawTexture(shape.getShapeTextureId(), graphics, x, y, w, h, 0, 0, 32, 32, 32, 32);
        GuiUtils.drawTexture(tex.getId(), graphics, x, y, w, h, 0, 0, width, height, width, height);
    }
        */


    /* DATA STORAGE */

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(NBT_WIDTH, width);
        tag.putInt(NBT_HEIGHT, height);
        tag.putInt(NBT_SHAPE, shape.getIndex());
        tag.putString(NBT_NAME, name);
        tag.putString(NBT_PIXEL_DATA, texture);
        return tag;
    }

    public static TrafficSignData fromNbt(CompoundTag tag) {
        TrafficSignData data = new TrafficSignData(tag.getInt(NBT_WIDTH), tag.getInt(NBT_HEIGHT), TrafficSignShape.getShapeByIndex(tag.getInt(NBT_SHAPE)));
        data.setName(tag.getString(NBT_NAME));
        data.setFromBase64(tag.getString(NBT_PIXEL_DATA));
        return data;
    }
    
    @Override
    public void close() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> ClientWrapper.clearTexture(this));
    }

    @Override
    protected void finalize() {
        this.close();
    }

    public static NamedTrafficSignTextureReference migrate(CompoundTag nbt) {
        TrafficSignData src = TrafficSignData.fromNbt(nbt);
        TrafficSignTextureData data = new TrafficSignTextureData(src.getShape(), Base64.getDecoder().decode(src.getTexture()), (short)src.getWidth(), (short)src.getHeight(), System.currentTimeMillis(), new UUID(0, 0));
        if (Platform.getEnvironment() == Env.CLIENT) {
            DataAccessor.getFromServer(data, ModAccessorTypes.CREATE_NEW_TRAFFIC_SIGN_TEXTURE, $ -> {});
        } else {
            data.save();
        }
        String name = src.getName();
        src.close();
        return NamedTrafficSignTextureReference.of(data, name);
    }
}

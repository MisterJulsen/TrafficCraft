package de.mrjulsen.trafficcraft.client;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.renderer.texture.DynamicTexture;

public class ModDynamicTexture extends DynamicTexture {

    public ModDynamicTexture(NativeImage pPixels) {
        super(pPixels);
    }

    public void setPixel(int x, int y, int color) {
        this.getPixels().setPixelRGBA(x, y, color);
    }

    public int getPixel(int x, int y) {
        return this.getPixels().getPixelRGBA(x, y);
    }
    
}

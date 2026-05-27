package de.mrjulsen.trafficcraft.data;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class SafeDynamicTexture extends DynamicTexture {

    private final int width;
    private final int height;

    private boolean closed = false;
    private final boolean immortal;

    public SafeDynamicTexture(NativeImage pixels) {
        this(pixels, false);
    }

    public SafeDynamicTexture(NativeImage image, boolean immortal) {
        super(image);
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.immortal = immortal;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public synchronized void close() {
        if (immortal || closed) {
            return;
        }
        closed = true;
        ClientWrapper.submitTaskAfterRenderFrame(super::close);
    }
}

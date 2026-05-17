package de.mrjulsen.trafficcraft.data;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class SafeDynamicTexture extends DynamicTexture {

    private boolean closed = false;
    private final boolean immortal;

    public SafeDynamicTexture(NativeImage pixels) {
        this(pixels, false);
    }

    public SafeDynamicTexture(NativeImage image, boolean immortal) {
        super(image);
        this.immortal = immortal;
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

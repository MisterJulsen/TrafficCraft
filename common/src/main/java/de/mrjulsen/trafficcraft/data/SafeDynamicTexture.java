package de.mrjulsen.trafficcraft.data;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class SafeDynamicTexture extends DynamicTexture {

    private boolean closed = false;

    public SafeDynamicTexture(NativeImage pixels) {
        super(pixels);
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        ClientWrapper.submitTaskAfterRenderFrame(super::close);
    }
}

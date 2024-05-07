package de.mrjulsen.legacydragonlib.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;

public class CustomRenderTarget extends RenderTarget {

    public CustomRenderTarget(boolean useDepth) {
        super(useDepth);
    }

    public static CustomRenderTarget create(Window mainWindow) {
        CustomRenderTarget framebuffer = new CustomRenderTarget(true);
        framebuffer.resize(mainWindow.getWidth(), mainWindow.getHeight(), Minecraft.ON_OSX);
        framebuffer.setClearColor(0, 0, 0, 0);
        framebuffer.enableStencil();
        return framebuffer;
    }

    public void renderWithAlpha(float alpha) {
        Window window = Minecraft.getInstance().getWindow();

        float vx = (float) window.getGuiScaledWidth();
        float vy = (float) window.getGuiScaledHeight();
        float tx = (float) viewWidth / (float) width;
        float ty = (float) viewHeight / (float) height;

        RenderSystem.enableDepthTest();
        RenderSystem.setShader(() -> Minecraft.getInstance().gameRenderer.blitShader);
        RenderSystem.getShader().setSampler("DiffuseSampler", colorTextureId);

        bindRead();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        bufferbuilder.vertex(0, vy, 0).color(1, 1, 1, alpha).uv(0, 0).endVertex();
        bufferbuilder.vertex(vx, vy, 0).color(1, 1, 1, alpha).uv(tx, 0).endVertex();
        bufferbuilder.vertex(vx, 0, 0).color(1, 1, 1, alpha).uv(tx, ty).endVertex();
        bufferbuilder.vertex(0, 0, 0).color(1, 1, 1, alpha).uv(0, ty).endVertex();

        tessellator.end();
        unbindRead();
    }

}

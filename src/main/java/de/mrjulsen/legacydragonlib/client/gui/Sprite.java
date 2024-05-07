package de.mrjulsen.legacydragonlib.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class Sprite {
    private final ResourceLocation texture;
    private final int textureId;

    private final int textureWidth;
    private final int textureHeight;
    private final int u;
    private final int v;
    private final int spriteWidth;
    private final int spriteHeight;    
    private final int renderWidth;
    private final int renderHeight;
    private final int renderOffsetX;
    private final int renderOffsetY;

    public Sprite(ResourceLocation texture, int textureWidth, int textureHeight, int u, int v, int spriteWidth, int spriteHeight) {
        this(texture, textureWidth, textureHeight, u, v, spriteWidth, spriteHeight, spriteWidth, spriteHeight);
    }

    public Sprite(ResourceLocation texture, int textureWidth, int textureHeight, int u, int v, int spriteWidth, int spriteHeight, int renderWidth, int renderHeight) {
        this(texture, textureWidth, textureHeight, u, v, spriteWidth, spriteHeight, renderWidth, renderHeight, 0, 0);
    }

    public Sprite(ResourceLocation texture, int textureWidth, int textureHeight, int u, int v, int spriteWidth, int spriteHeight, int renderWidth, int renderHeight, int renderOffsetX, int renderOffsetY) {
        this.texture = texture;
        this.textureId = 0;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.u = u;
        this.v = v;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.renderWidth = renderWidth;
        this.renderHeight = renderHeight;
        this.renderOffsetX = renderOffsetX;
        this.renderOffsetY = renderOffsetY;
    }

    public Sprite(int textureId, int textureWidth, int textureHeight, int u, int v, int spriteWidth, int spriteHeight) {
        this(textureId, textureWidth, textureHeight, u, v, spriteWidth, spriteHeight, spriteWidth, spriteHeight);
    }

    public Sprite(int textureId, int textureWidth, int textureHeight, int u, int v, int spriteWidth, int spriteHeight, int renderWidth, int renderHeight) {
        this(textureId, textureWidth, textureHeight, u, v, spriteWidth, spriteHeight, renderWidth, renderHeight, 0, 0);
    }

    public Sprite(int textureId, int textureWidth, int textureHeight, int u, int v, int spriteWidth, int spriteHeight, int renderWidth, int renderHeight, int renderOffsetX, int renderOffsetY) {
        this.texture = null;
        this.textureId = textureId;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.u = u;
        this.v = v;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.renderWidth = renderWidth;
        this.renderHeight = renderHeight;
        this.renderOffsetX = renderOffsetX;
        this.renderOffsetY = renderOffsetY;
    }


    public static Sprite empty() {
        return new Sprite(TextureManager.INTENTIONAL_MISSING_TEXTURE, 0, 0, 0, 0, 0, 0);
    }

    public void render(GuiGraphics graphics, int x, int y) {
        if (texture == null) {
            GuiUtils.blit(textureId, graphics, x + renderOffsetX, y + renderOffsetY, renderWidth, renderHeight, u, v, spriteWidth, spriteHeight, textureWidth, textureHeight);            
        } else {
            GuiUtils.blit(texture, graphics, x + renderOffsetX, y + renderOffsetY, renderWidth, renderHeight, u, v, spriteWidth, spriteHeight, textureWidth, textureHeight);
        }
    }

    public int getWidth() {
        return renderWidth;
    }

    public int getHeight() {
        return renderHeight;
    }

    public ResourceLocation getTextureLocation() {
        return texture;
    }

    public int getTextureId() {
        return textureId;
    }
}

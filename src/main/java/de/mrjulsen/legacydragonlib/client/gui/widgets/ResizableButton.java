package de.mrjulsen.legacydragonlib.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;

import de.mrjulsen.legacydragonlib.client.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResizableButton extends Button {

    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    public ResizableButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, Mth.clamp(pWidth, 0, 394), Mth.clamp(pHeight, 0, 34), pMessage, pOnPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        GuiUtils.setTexture(WIDGETS_LOCATION);
        GuiUtils.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getTextureY();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        GuiUtils.blit(WIDGETS_LOCATION, graphics, this.getX(), this.getY(), 0, 46 + i * 20, this.width / 2, this.height / 2);
        GuiUtils.blit(WIDGETS_LOCATION, graphics, this.getX() + this.width / 2, this.getY(), 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height / 2);       
        GuiUtils.blit(WIDGETS_LOCATION, graphics, this.getX(), this.getY() + this.height / 2, 0, 46 + (i + 1) * 20 - this.height / 2, this.width / 2, this.height / 2);
        GuiUtils.blit(WIDGETS_LOCATION, graphics, this.getX() + this.width / 2, this.getY() + this.height / 2, 200 - this.width / 2, 46 + (i + 1) * 20 - this.height / 2, this.width / 2, this.height / 2);

        int k = getFGColor();
        this.renderString(graphics, minecraft.font, k | Mth.ceil(this.alpha * 255.0F) << 24);
        int j = getFGColor();
        graphics.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    private int getTextureY() {
        int i = 1;
        if (!this.active) {
           i = 0;
        } else if (this.isHovered()) {
           i = 2;
        }
  
        return i;
     }
    
}

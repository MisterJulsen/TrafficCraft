package de.mrjulsen.legacydragonlib.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.legacydragonlib.client.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
        super(pX, pY, Mth.clamp(pWidth, 0, 394), Mth.clamp(pHeight, 0, 34), pMessage, pOnPress);
    }

    public ResizableButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, Button.OnPress pOnPress, Button.OnTooltip pOnTooltip) {
        super(pX, pY, Mth.clamp(pWidth, 0, 394), Mth.clamp(pHeight, 0, 34), pMessage, pOnPress, pOnTooltip);
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        GuiUtils.setTexture(WIDGETS_LOCATION);
        GuiUtils.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        GuiUtils.blit(WIDGETS_LOCATION, pPoseStack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height / 2);
        GuiUtils.blit(WIDGETS_LOCATION, pPoseStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height / 2);       
        GuiUtils.blit(WIDGETS_LOCATION, pPoseStack, this.x, this.y + this.height / 2, 0, 46 + (i + 1) * 20 - this.height / 2, this.width / 2, this.height / 2);
        GuiUtils.blit(WIDGETS_LOCATION, pPoseStack, this.x + this.width / 2, this.y + this.height / 2, 200 - this.width / 2, 46 + (i + 1) * 20 - this.height / 2, this.width / 2, this.height / 2);

        this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
        int j = getFGColor();
        drawCenteredString(pPoseStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
    }
    
}

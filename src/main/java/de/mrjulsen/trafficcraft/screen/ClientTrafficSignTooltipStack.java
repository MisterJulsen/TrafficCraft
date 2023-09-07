package de.mrjulsen.trafficcraft.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import de.mrjulsen.trafficcraft.data.TrafficSignData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientTrafficSignTooltipStack implements ClientTooltipComponent {
    private static final int IMAGE_HEIGHT = 18;
    private final NonNullList<TrafficSignData> data;
    private final int selectedIndex;

    public ClientTrafficSignTooltipStack(TrafficSignTooltip pTrafficSignTooltip) {
        this.data = pTrafficSignTooltip.getData();
        this.selectedIndex = pTrafficSignTooltip.getSelectedIndex();
    }

    public int getHeight() {
        return this.data.size() * 18;
    }

    public int getWidth(Font pFont) {
        int maxWidth = 0;
        for (int i = 0; i < this.data.size(); i++) {
            String label = (selectedIndex == i ?"> " : "") + this.data.get(i).getName();
            int textWidth = pFont.width(new TextComponent(label).withStyle(selectedIndex == i ? ChatFormatting.BOLD : ChatFormatting.RESET));
            if (maxWidth < textWidth + IMAGE_HEIGHT + 5) {
                maxWidth = textWidth + IMAGE_HEIGHT + 5;
            }
        }
        return maxWidth;
    }

    public void renderImage(Font pFont, int pMouseX, int pMouseY, PoseStack pPoseStack, ItemRenderer pItemRenderer, int pBlitOffset) {
        for (int i = 0; i < this.data.size(); i++) {
            DynamicTexture texture = this.data.get(i).getDynamicTexture();
            int w = texture.getPixels().getWidth();
            int h = texture.getPixels().getHeight();

            RenderSystem.setShaderTexture(0, texture.getId());
            GuiComponent.blit(pPoseStack, pMouseX + 0, pMouseY + i * IMAGE_HEIGHT, 16, 16, 0, 0, w, h, w, h);
        }
    }

    @Override
    public void renderText(Font pFont, int pX, int pY, Matrix4f pMatrix4f, BufferSource pBufferSource) {
        ClientTooltipComponent.super.renderText(pFont, pX, pY, pMatrix4f, pBufferSource);
        for (int i = 0; i < this.data.size(); i++) {
            String label = (selectedIndex == i ?"> " : "") + this.data.get(i).getName();
            pFont.drawInBatch(new TextComponent(label).withStyle(selectedIndex == i ? ChatFormatting.BOLD : ChatFormatting.RESET).withStyle(selectedIndex == i ? ChatFormatting.WHITE : ChatFormatting.GRAY), pX + 3 + IMAGE_HEIGHT, pY + i * IMAGE_HEIGHT + IMAGE_HEIGHT / 2 - pFont.lineHeight / 2, 16777215, true, pMatrix4f, pBufferSource, false, 16777215, 16777215);
        }
    }
}

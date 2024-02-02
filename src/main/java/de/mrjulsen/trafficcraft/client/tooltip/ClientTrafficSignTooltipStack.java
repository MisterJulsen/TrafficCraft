package de.mrjulsen.trafficcraft.client.tooltip;

import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.client.TrafficSignTextureCacheClient;
import de.mrjulsen.trafficcraft.data.TrafficSignData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientTrafficSignTooltipStack implements ClientTooltipComponent {
    private static final int IMAGE_HEIGHT = 18;
    private final NonNullList<TrafficSignData> data;
    private final int selectedIndex;
    private static final float FONT_SCALE = 0.75f;

    public ClientTrafficSignTooltipStack(TrafficSignTooltip pTrafficSignTooltip) {
        this.data = pTrafficSignTooltip.getData();
        this.selectedIndex = pTrafficSignTooltip.getSelectedIndex();
    }

    public int getHeight() {
        return (this.data.size() > 12 ? 12 : this.data.size()) * 18;
    }

    public int getWidth(Font pFont) {
        int maxWidth = 0;
        if (true) { //(Screen.hasShiftDown()) {
            int w = 0;
            for (int i = 0; i < this.data.size(); i++) {
                if (i % 12 == 0) {
                    maxWidth += w;
                    w = 0;
                }
                String label = (selectedIndex == i ?"> " : "") + this.data.get(i).getName();
                int textWidth = (int)(pFont.width(Utils.text(label).withStyle(selectedIndex == i ? ChatFormatting.BOLD : ChatFormatting.RESET)) * FONT_SCALE);
                if (w < textWidth + IMAGE_HEIGHT + 10) {
                    w = textWidth + IMAGE_HEIGHT + 10;
                }
            }
            maxWidth += w;
        }
        return maxWidth;
    }

    @Override
    public void renderImage(Font pFont, int pX, int pY, GuiGraphics graphics) {
        int x = pX;
        int y = pY - IMAGE_HEIGHT;
        int maxW = 0;
        for (int i = 0; i < this.data.size(); i++) {
            y += IMAGE_HEIGHT;

            if (i % 12 == 0 && i != 0) {
                x += maxW + IMAGE_HEIGHT + 10;
                maxW = 0;
                y = 0;
            }

            final int j = i;
            DynamicTexture texture = TrafficSignTextureCacheClient.getTexture(this.data.get(j), this.data.get(j).getTexture(), false, (tex) -> {
                this.data.get(j).setFromBase64(TrafficSignTextureCacheClient.textureToBase64(this.data.get(j)));
            });
            int w = texture.getPixels().getWidth();
            int h = texture.getPixels().getHeight();

            GuiUtils.blit(texture.getId(), graphics, x, y, 16, 16, 0, 0, w, h, w, h);
            
            if (true) { //(Screen.hasShiftDown()) {
                String txt = (selectedIndex == i ?"> " : "") + this.data.get(i).getName();
                MutableComponent label = Utils.text(txt).withStyle(selectedIndex == i ? ChatFormatting.BOLD : ChatFormatting.RESET).withStyle(selectedIndex == i ? ChatFormatting.WHITE : ChatFormatting.GRAY);
                int fW = (int)(pFont.width(label) * FONT_SCALE);
                if (maxW < fW) {
                    maxW = fW;
                }
                graphics.pose().scale(FONT_SCALE, FONT_SCALE, FONT_SCALE);
                graphics.pose().translate(0, 0, 1000);
                
                graphics.drawString(
                    pFont,
                    label.getString(),
                    (3 + IMAGE_HEIGHT + x) / FONT_SCALE,
                    (y + IMAGE_HEIGHT / 2 - pFont.lineHeight / 2) / FONT_SCALE,
                    16777215,
                    false
                );
                graphics.pose().setIdentity();
            }
            this.data.get(j).close();
        }
    }
}

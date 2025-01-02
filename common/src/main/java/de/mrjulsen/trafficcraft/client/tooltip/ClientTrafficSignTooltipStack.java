package de.mrjulsen.trafficcraft.client.tooltip;

import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.data.DataCache;
import de.mrjulsen.mcdragonlib.data.Pair;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.data.TrafficSignClientTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.NonNullList;

public class ClientTrafficSignTooltipStack implements ClientTooltipComponent {

    private final NonNullList<NamedTrafficSignTextureReference> patterns;
    private final NamedTrafficSignTextureReference selectedData;
    private final Map<NamedTrafficSignTextureReference, TrafficSignClientTexture> textures;

    private static final float FONT_SCALE = 0.75f;

    private int lastKnownTexturesCount = 0;
    private final DataCache<Pair<Integer, Integer>, Integer> gridLayout = new DataCache<>(n -> {
        if (n <= 0) {
            return Pair.of(1, n);
        }
        int sqrt = (int)Math.sqrt(n.doubleValue());
        int width = (int)Math.ceil((double)n / (double)sqrt);
        return Pair.of(width, sqrt);
    });

    public ClientTrafficSignTooltipStack(TrafficSignTooltip pTrafficSignTooltip) {
        this.patterns = pTrafficSignTooltip.getPatterns();
        this.selectedData = pTrafficSignTooltip.getSelected();
        this.textures = pTrafficSignTooltip.getTextures();
    }

    public int getHeight() {
        checkGridLayout();
        return gridLayout.get(lastKnownTexturesCount).getSecond() * 18 + (selectedData == null ? 0 : Minecraft.getInstance().font.lineHeight * 2 + 24);
    }

    public int getWidth(Font pFont) {
        checkGridLayout();
        return gridLayout.get(lastKnownTexturesCount).getFirst() * 18;
    }

    private void checkGridLayout() {
        if (lastKnownTexturesCount != patterns.size()) {
            lastKnownTexturesCount = patterns.size();
            gridLayout.clear();
        }
    }

    public void renderImage(Font pFont, int pMouseX, int pMouseY, PoseStack pPoseStack, ItemRenderer pItemRenderer, int pBlitOffset) {
        checkGridLayout();
        Graphics graphics = new Graphics(pPoseStack);

        Pair<Integer, Integer> grid = gridLayout.get(lastKnownTexturesCount);
        int x = pMouseX;
        int y = pMouseY;

        graphics.poseStack().pushPose();
        graphics.poseStack().scale(FONT_SCALE, FONT_SCALE, FONT_SCALE);
        graphics.poseStack().translate(0, 0, 1000);
        if (selectedData != null) {            
            graphics.poseStack().pushPose();
            graphics.poseStack().translate((x + 5) / FONT_SCALE, y / FONT_SCALE, 0);
            GuiUtils.drawString(graphics, pFont, 0, 0, TextUtils.translate("item.trafficcraft.pattern_catalogue.tooltip.selected_texture"), 0xFFDBDBDB, EAlignment.LEFT, false);
            GuiUtils.drawString(graphics, pFont, 32, pFont.lineHeight + 10, selectedData.getName(), 0xFFFFFFFF, EAlignment.LEFT, false);
            graphics.poseStack().popPose();
        }
        if (lastKnownTexturesCount > 0) {            
            graphics.poseStack().pushPose();
            graphics.poseStack().translate((x + 5) / FONT_SCALE, (y + pFont.lineHeight + 24) / FONT_SCALE, 0);
            GuiUtils.drawString(graphics, pFont, 0, 0, TextUtils.translate("item.trafficcraft.pattern_catalogue.tooltip.saved_textures"), 0xFFDBDBDB, EAlignment.LEFT, false);
            graphics.poseStack().popPose();
        }
        graphics.poseStack().popPose();

        if (selectedData != null) {
            renderTexture(pPoseStack, x + 10, y + pFont.lineHeight, selectedData);
        }

        y += pFont.lineHeight * 2 + 24;
        for (int i = 0, k = 0; i < grid.getFirst() && k < lastKnownTexturesCount; i++) {
            for (int j = 0; j < grid.getSecond() && k < lastKnownTexturesCount; j++, k++) {
                final int n = k;      
                final NamedTrafficSignTextureReference textureData = this.patterns.get(n);
                renderTexture(pPoseStack, x + 10 + (i * 18), y + (j * 18), textureData);
            }
        }
    }    

    private void renderTexture(PoseStack poseStack, int x, int y, NamedTrafficSignTextureReference data) {
        TrafficSignClientTexture texture = textures.get(data);
        if (texture != null) {
            int w = texture.getRawData().getWidth();
            int h = texture.getRawData().getHeight();
            RenderSystem.setShaderTexture(0, texture.getTextureLocation());
            GuiComponent.blit(poseStack, x, y, 16, 16, 0, 0, w, h, w, h);
        }
    }
}

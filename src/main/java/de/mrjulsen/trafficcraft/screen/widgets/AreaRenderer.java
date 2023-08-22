package de.mrjulsen.trafficcraft.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

public class AreaRenderer {

    private static final ResourceLocation UI = new ResourceLocation(ModMain.MOD_ID, "textures/gui/ui.png"); 
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;
    private static final int UI_SECTION_SIZE = 5;

    public static void renderGrayArea(PoseStack poseStack, int x, int y, int w, int h, GrayAreaStyle style) {
        RenderSystem.setShaderTexture(0, UI);

        int startU = 10, startV = 5;
        startU += style.getIndex() * UI_SECTION_SIZE;
        GuiComponent.blit(poseStack, x, y, 2, 2, startU, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top left
        GuiComponent.blit(poseStack, x, y + h - 2, 2, 2, startU, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom left
        GuiComponent.blit(poseStack, x + w - 2, y, 2, 2, startU + 3, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top right
        GuiComponent.blit(poseStack, x + w - 2, y + h - 2, 2, 2, startU + 3, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom right

        GuiComponent.blit(poseStack, x + 2, y, w - 4, 2, startU + 2, startV, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top
        GuiComponent.blit(poseStack, x + 2, y + h - 2, w - 4, 2, startU + 2, startV + 3, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom
        GuiComponent.blit(poseStack, x, y + 2, 2, h - 4, startU, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // left
        GuiComponent.blit(poseStack, x + w - 2, y + 2, 2, h - 4, startU + 3, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // right
        
        GuiComponent.blit(poseStack, x + 2, y + 2, w - 4, h - 4, startU + 2, startV + 2, 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static void renderBrownArea(PoseStack poseStack, int x, int y, int w, int h, BrownAreaStyle style) {
        RenderSystem.setShaderTexture(0, UI);

        int startU = 0, startV = 0;
        startU += style.getIndex() * UI_SECTION_SIZE;
        GuiComponent.blit(poseStack, x, y, 2, 2, startU, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top left
        GuiComponent.blit(poseStack, x, y + h - 2, 2, 2, startU, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom left
        GuiComponent.blit(poseStack, x + w - 2, y, 2, 2, startU + 3, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top right
        GuiComponent.blit(poseStack, x + w - 2, y + h - 2, 2, 2, startU + 3, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom right

        GuiComponent.blit(poseStack, x + 2, y, w - 4, 2, startU + 2, startV, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top
        GuiComponent.blit(poseStack, x + 2, y + h - 2, w - 4, 2, startU + 2, startV + 3, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom
        GuiComponent.blit(poseStack, x, y + 2, 2, h - 4, startU, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // left
        GuiComponent.blit(poseStack, x + w - 2, y + 2, 2, h - 4, startU + 3, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // right
        
        GuiComponent.blit(poseStack, x + 2, y + 2, w - 4, h - 4, startU + 2, startV + 2, 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static enum BrownAreaStyle {
        BUTTON(0),
        SELECTED(1),
        SUNKEN(2),
        RAISED(3);

        private int index;

        private BrownAreaStyle(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public static enum GrayAreaStyle {
        SUNKEN(0),
        RAISED(1);

        private int index;

        private GrayAreaStyle(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}

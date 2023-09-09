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

    public static void renderArea(PoseStack poseStack, int x, int y, int w, int h, ColorStyle color, AreaStyle style) {
        RenderSystem.setShaderTexture(0, UI);

        int startU = 0, startV = color.getIndex() * 5;
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

    public static void renderWindow(PoseStack poseStack, int x, int y, int w, int h) {
        RenderSystem.setShaderTexture(0, UI);
        
        int startU = 0, startV = 10;

        GuiComponent.blit(poseStack, x, y, 4, 4, startU, startV, 4, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top left
        GuiComponent.blit(poseStack, x, y + h - 4, 4, 4, startU, startV + 6, 4, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom left
        GuiComponent.blit(poseStack, x + w - 4, y, 4, 4, startU + 6, startV, 4, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top right
        GuiComponent.blit(poseStack, x + w - 4, y + h - 4, 4, 4, startU + 6, startV + 6, 4, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom right

        GuiComponent.blit(poseStack, x + 4, y, w, 4, startU + 4, startV, 2, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top
        GuiComponent.blit(poseStack, x + 4, y + h - 4, w, 4, startU + 4, startV + 6, 2, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom
        GuiComponent.blit(poseStack, x, y + 4, 4, h, startU, startV + 4, 4, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // left
        GuiComponent.blit(poseStack, x + w - 4, y + 4, 4, h, startU + 6, startV + 4, 4, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // right
        
        GuiComponent.blit(poseStack, x + 4, y + 4, w, h, startU + 4, startV + 4, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static enum AreaStyle {
        BUTTON(0),
        SELECTED(1),
        SUNKEN(2),
        RAISED(3);

        private int index;

        private AreaStyle(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public static enum ColorStyle {
        BROWN(0),
        GRAY(1);

        private int index;

        private ColorStyle(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}

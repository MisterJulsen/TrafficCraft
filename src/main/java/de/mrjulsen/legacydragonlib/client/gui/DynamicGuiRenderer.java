package de.mrjulsen.legacydragonlib.client.gui;

import de.mrjulsen.legacydragonlib.DragonLibConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class DynamicGuiRenderer {

    protected static final ResourceLocation NATIVE_WIDGETS = new ResourceLocation("textures/gui/widgets.png"); 
    protected static final ResourceLocation UI = new ResourceLocation(DragonLibConstants.DRAGONLIB_MODID, "textures/gui/ui.png"); 
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;
    protected static final int UI_SECTION_SIZE = 5;

    public static void renderArea(GuiGraphics graphics, GuiAreaDefinition area, AreaStyle style, ButtonState state) {
        renderArea(graphics, area.getLeft(), area.getTop(), area.getWidth(), area.getHeight(), style, state);
    }

    public static void renderArea(GuiGraphics graphics, int x, int y, int w, int h, AreaStyle color, ButtonState style) {

        if (color == AreaStyle.NATIVE) {
            int i = 0;
            switch (style) {
                case SELECTED:
                    i = 2;
                    break;
                case SUNKEN:
                case RAISED:
                    i = 0;
                    break;
                default:
                    i = 1;
                    break;
            }

            GuiUtils.blit(NATIVE_WIDGETS, graphics, x, y, 0, 46 + i * 20, w / 2, h / 2);
            GuiUtils.blit(NATIVE_WIDGETS, graphics, x + w / 2, y, 200 - w / 2, 46 + i * 20, w / 2, h / 2);       
            GuiUtils.blit(NATIVE_WIDGETS, graphics, x, y + h / 2, 0, 46 + (i + 1) * 20 - h / 2, w / 2, h / 2);
            GuiUtils.blit(NATIVE_WIDGETS, graphics, x + w / 2, y + h / 2, 200 - w / 2, 46 + (i + 1) * 20 - h / 2, w / 2, h / 2);
        }

        int startU = 0, startV = color.getIndex() * 5;
        startU += style.getIndex() * UI_SECTION_SIZE;
        GuiUtils.blit(UI, graphics, x, y, 2, 2, startU, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top left
        GuiUtils.blit(UI, graphics, x, y + h - 2, 2, 2, startU, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom left
        GuiUtils.blit(UI, graphics, x + w - 2, y, 2, 2, startU + 3, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top right
        GuiUtils.blit(UI, graphics, x + w - 2, y + h - 2, 2, 2, startU + 3, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom right

        GuiUtils.blit(UI, graphics, x + 2, y, w - 4, 2, startU + 2, startV, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top
        GuiUtils.blit(UI, graphics, x + 2, y + h - 2, w - 4, 2, startU + 2, startV + 3, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom
        GuiUtils.blit(UI, graphics, x, y + 2, 2, h - 4, startU, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // left
        GuiUtils.blit(UI, graphics, x + w - 2, y + 2, 2, h - 4, startU + 3, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // right
        
        GuiUtils.blit(UI, graphics, x + 2, y + 2, w - 4, h - 4, startU + 2, startV + 2, 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static void renderContainerBackground(GuiGraphics graphics, GuiAreaDefinition area) {
        renderContainerBackground(graphics, area.getLeft(), area.getTop(), area.getWidth(), area.getHeight());
    }

    public static void renderContainerBackground(GuiGraphics graphics, int x, int y, int w, int h) {
        int startU = 10, startV = 15;
        GuiUtils.blit(UI, graphics, x, y, 2, 2, startU, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top left
        GuiUtils.blit(UI, graphics, x, y + h - 2, 2, 2, startU, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom left
        GuiUtils.blit(UI, graphics, x + w - 2, y, 2, 2, startU + 3, startV, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top right
        GuiUtils.blit(UI, graphics, x + w - 2, y + h - 2, 2, 2, startU + 3, startV + 3, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom right

        GuiUtils.blit(UI, graphics, x + 2, y, w - 4, 2, startU + 2, startV, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top
        GuiUtils.blit(UI, graphics, x + 2, y + h - 2, w - 4, 2, startU + 2, startV + 3, 1, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom
        GuiUtils.blit(UI, graphics, x, y + 2, 2, h - 4, startU, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // left
        GuiUtils.blit(UI, graphics, x + w - 2, y + 2, 2, h - 4, startU + 3, startV + 2, 2, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT); // right
        
        GuiUtils.blit(UI, graphics, x + 2, y + 2, w - 4, h - 4, startU + 2, startV + 2, 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static void renderWindow(GuiGraphics graphics, GuiAreaDefinition area) {
        renderWindow(graphics, area.getLeft(), area.getTop(), area.getWidth(), area.getHeight());
    }

    public static void renderWindow(GuiGraphics graphics, int x, int y, int w, int h) {
        int startU = 0, startV = 15;

        GuiUtils.blit(UI, graphics, x, y, 4, 4, startU, startV, 4, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top left
        GuiUtils.blit(UI, graphics, x, y + h - 4, 4, 4, startU, startV + 6, 4, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom left
        GuiUtils.blit(UI, graphics, x + w - 4, y, 4, 4, startU + 6, startV, 4, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top right
        GuiUtils.blit(UI, graphics, x + w - 4, y + h - 4, 4, 4, startU + 6, startV + 6, 4, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom right

        GuiUtils.blit(UI, graphics, x + 4, y, w - 8, 4, startU + 4, startV, 2, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // top
        GuiUtils.blit(UI, graphics, x + 4, y + h - 4, w - 8, 4, startU + 4, startV + 6, 2, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT); // bottom
        GuiUtils.blit(UI, graphics, x, y + 4, 4, h - 8, startU, startV + 4, 4, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // left
        GuiUtils.blit(UI, graphics, x + w - 4, y + 4, 4, h - 8, startU + 6, startV + 4, 4, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT); // right
        
        GuiUtils.blit(UI, graphics, x + 4, y + 4, w - 8, h - 8, startU + 4, startV + 4, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static enum ButtonState {
        BUTTON(0),
        SELECTED(1),
        SUNKEN(2),
        RAISED(3);

        private int index;

        private ButtonState(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public static enum AreaStyle {
        NATIVE(-1),
        BROWN(0),
        GRAY(1),
        RED(2);

        private int index;

        private AreaStyle(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}


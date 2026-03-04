package de.mrjulsen.trafficcraft.client.screen.workbench;

import org.lwjgl.glfw.GLFW;

import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils.TextureFillMode;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.client.screen.workbench.EditorScreen.TrafficSignWorkbenchEditorTool;

public class Canvas extends DLGuiComponent {

    public static class EditorConfig {
        public TrafficSignWorkbenchEditorTool tool;
        public DLColor color;
    }

    private final TrafficSignShape shape;
    private final EditorConfig config;
    
    public int[][] pixels;

    public Canvas(int x, int y, TrafficSignShape shape, EditorConfig config) {
        super(x, y, 130, 130);
        this.shape = shape;
        this.config = config;

        addEventListener(DLGuiStandardEvents.DragEvent.class, (s, e) -> {
            draw(e.mouseX(), e.mouseY(), e.button());
            return false;
        });

        cursor.set(CursorType.CROSSHAIR);
        clearCanvas();
    }

    private void draw(double mouseX, double mouseY, int button) {
        int x = (int)((mouseX - 1) / 4);
        int y = (int)((mouseY - 1) / 4);
        
        switch (config.tool) {
            case DRAW:
            case ERASER:
            default:
                int color = config.color.getAsARGB();
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || config.tool == TrafficSignWorkbenchEditorTool.ERASER) {
                    color = 0;
                }

                if (shape.isPixelValid(x, y)) {
                    pixels[x][y] = color;
                }
                break;
            case PICK_COLOR:
                if (shape.isPixelValid(x, y) && pixels[x][y] != 0) {
                    config.color = DLColor.fromInt(pixels[x][y]);
                }
                break;
            case FILL:
                if (shape.isPixelValid(x, y)) {
                    fillArea(x, y, pixels[x][y]);
                    /*
                    config.tool = TrafficSignWorkbenchEditorTool.DRAW;
                    for (int w = 0; w < groupEditorToolbar1.components.size(); w++) {
                        if (groupEditorToolbar1.components.get(w) instanceof DLIconButton btn) {
                            if (w == 0) {
                                btn.select();
                            } else {
                                btn.deselect();
                            }
                        }
                    }
                        */
                }
                break;
        }
    }

    private void fillArea(int x, int y, final int replaceColor) {
        if (shape.isPixelValid(x, y) && pixels[x][y] != config.color.getAsARGB()) {
            pixels[x][y] = config.color.getAsARGB();
        } else {
            return;
        }

        int x1 = x - 1;
        if (x1 >= 0 && shape.isPixelValid(x1, y) && pixels[x1][y] == replaceColor)
            fillArea(x - 1, y, replaceColor);
        x1 = x + 1;
        if (x1 < TrafficSignShape.MAX_WIDTH && shape.isPixelValid(x1, y) && pixels[x1][y] == replaceColor)
            fillArea(x + 1, y, replaceColor);
        int y1 = y - 1;
        if (y1 >= 0 && shape.isPixelValid(x, y1) && pixels[x][y1] == replaceColor)
            fillArea(x, y - 1, replaceColor);
        y1 = y + 1;
        if (y1 < TrafficSignShape.MAX_HEIGHT && shape.isPixelValid(x, y1) && pixels[x][y1] == replaceColor)
            fillArea(x, y + 1, replaceColor);
    }

    private void clearCanvas() {
        pixels = new int[TrafficSignShape.MAX_WIDTH][];
        for (int a = 0; a < pixels.length; a++) {
            pixels[a] = new int[TrafficSignShape.MAX_HEIGHT];
        }
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.setTint(DLColor.BLACK);
        GuiUtils.drawTexture(shape.getShapeTextureId(), graphics, 0, 0, width() - 2, height() - 2, 0, 0, 32, 32, TextureFillMode.STRETCH, 32, 32);
        GuiUtils.drawTexture(shape.getShapeTextureId(), graphics, 2, 0, width() - 2, height() - 2, 0, 0, 32, 32, TextureFillMode.STRETCH, 32, 32);
        GuiUtils.drawTexture(shape.getShapeTextureId(), graphics, 0, 2, width() - 2, height() - 2, 0, 0, 32, 32, TextureFillMode.STRETCH, 32, 32);
        GuiUtils.drawTexture(shape.getShapeTextureId(), graphics, 2, 2, width() - 2, height() - 2, 0, 0, 32, 32, TextureFillMode.STRETCH, 32, 32);
        GuiUtils.resetTint();
        GuiUtils.drawTexture(shape.getShapeTextureId(), graphics, 1, 1, width() - 2, height() - 2, 0, 0, 32, 32, TextureFillMode.STRETCH, 32, 32);
        
        for (int a = 0; a < pixels.length; a++) {
            for (int b = 0; b < pixels[a].length; b++) {
                GuiUtils.fill(graphics,
                    1 + a * 4,
                    1 + b * 4,
                    4,
                    4,
                    DLColor.fromInt(pixels[a][b]));
            }
        }

        if (isSelected()) {
            int mX = (int)((mouseX - 1) / 4);
            int mY = (int)((mouseY - 1) / 4);
            if (shape.isPixelValid(mX, mY)) {
                int x = mX * 4 + 1;
                int y = mY * 4 + 1;
                GuiUtils.fill(graphics, x, y, 4, 4, DLColor.fromInt(0x7F000000));
            }
        }
    }
    
}

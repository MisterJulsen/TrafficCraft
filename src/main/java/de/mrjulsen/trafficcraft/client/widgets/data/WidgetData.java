package de.mrjulsen.trafficcraft.client.widgets.data;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;

public class WidgetData {
    private AbstractWidget widget;
    private int x, y;

    public WidgetData(AbstractWidget widget) {
        this.widget = widget;
        this.x = widget.getX();
        this.y = widget.getY();
    }

    public AbstractWidget getWidget() {
        return this.widget;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setPosOffset(int xOffset, int yOffset) {
        this.widget.setX(this.x + xOffset);
        this.widget.setY(this.y + yOffset);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.widget.render(graphics, mouseX, mouseY, partialTicks);
    }

    public void renderWithOffset(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, int xOffset, int yOffset) {
        this.setPosOffset(xOffset, yOffset);
        this.widget.render(graphics, mouseX, mouseY, partialTicks);
    }
}

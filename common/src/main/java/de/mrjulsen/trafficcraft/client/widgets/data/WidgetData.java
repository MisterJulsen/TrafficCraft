package de.mrjulsen.trafficcraft.client.widgets.data;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;

public class WidgetData {
    private AbstractWidget widget;
    private int x, y;

    public WidgetData(AbstractWidget widget) {
        this.widget = widget;
        this.x = widget.x;
        this.y = widget.y;
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
        this.widget.x = this.x + xOffset;
        this.widget.y = this.y + yOffset;
    }

    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        this.widget.render(stack, mouseX, mouseY, partialTicks);
    }

    public void renderWithOffset(PoseStack stack, int mouseX, int mouseY, float partialTicks, int xOffset, int yOffset) {
        this.setPosOffset(xOffset, yOffset);
        this.widget.render(stack, mouseX, mouseY, partialTicks);
    }
}

package de.mrjulsen.trafficcraft.screen.widgets;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer.AreaStyle;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer.ColorStyle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HScrollBar extends WidgetBase implements ICustomAreaControl {

    private static final ResourceLocation UI = new ResourceLocation(ModMain.MOD_ID, "textures/gui/ui.png");  

    private final GuiAreaDefinition scrollArea;

    private double scrollPercentage;
    private int scroll;
    private int maxScroll = 2;
    private boolean isScrolling = false;

    private int maxRowsOnPage = 1;
    private int scrollerHeight = 15;

    // Events
    public Consumer<HScrollBar> onValueChanged;

    public HScrollBar(int x, int y, int w, int h, GuiAreaDefinition scrollArea) {
        super(x, y, Math.max(7, w), Math.max(7, h), null);
        this.scrollArea = scrollArea;
    }

    public HScrollBar(int x, int y, int h, GuiAreaDefinition scrollArea) {
        this(x, y, 14, h, scrollArea);
    }

    public HScrollBar(int x, int y, int w, int h) {
        this(x, y, w, h, null);
    }

    public HScrollBar(int x, int y, int h) {
        this(x, y, h, null);
    }

    public HScrollBar setMaxRowsOnPage(int c) {
        maxRowsOnPage = Math.max(1, c);
        return this;
    }

    public HScrollBar setScrollerHeight(int h) {
        scrollerHeight = Math.max(5, h);
        return this;
    }

    public HScrollBar updateMaxScroll(int rows) {
        this.maxScroll = Math.max(rows - maxRowsOnPage, 0);
        this.scrollerHeight = (int)(height / Math.max(rows / (float)maxRowsOnPage, 1.0f));
        return this;
    }

    public HScrollBar setOnValueChangedEvent(Consumer<HScrollBar> event) {
        this.onValueChanged = event;
        return this;
    }

    public int getScrollValue() {
        return scroll;
    }

    public int getMaxScroll() {
        return maxScroll;
    }

    public int getMaxRowsOnPage() {
        return maxRowsOnPage;
    }



    @Override
    public boolean onClick(double pMouseX, double pMouseY) {
        if (isMouseOver(pMouseX, pMouseY) && canScroll()) {
            isScrolling = true;
            scrollTo(pMouseY);
        }
        return true;
    }

    @Override
    protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
        if (this.isScrolling) {
            scrollTo(pMouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (canScroll()) {
            scroll = Mth.clamp((int)(scroll - pDelta), 0, maxScroll);

            int i = maxScroll;
            this.scrollPercentage = (double)this.scrollPercentage - pDelta / (double)i;
            this.scrollPercentage = Mth.clamp(this.scrollPercentage, 0.0F, 1.0F);
            
            if (onValueChanged != null)
                onValueChanged.accept(this);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRelease(double pMouseX, double pMouseY) {
        this.isScrolling = false;
    }

    private void scrollTo(double mousePos) {
        int i = y + 1;
        int j = i + height - 2;

        this.scrollPercentage = (mousePos - (double)i - ((double)scrollerHeight / 2.0D)) / (double)(j - i - scrollerHeight);
        this.scrollPercentage = Mth.clamp(this.scrollPercentage, 0.0F, 1.0F);
        scroll = Math.max(0, (int)Math.round(scrollPercentage * maxScroll));
        
        if (onValueChanged != null)
            onValueChanged.accept(this);
    }

    public boolean canScroll() {
        return maxScroll > 0;
    }

    @Override
    public void renderWidget(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderTexture(0, UI);

        // Render background
        AreaRenderer.renderArea(pPoseStack, x, y, width, height, ColorStyle.GRAY, AreaStyle.SUNKEN);

        // Render scrollbar
        int startU = canScroll() ? 20 : 25;
        int startV = 5;

        int x1 = x + 1;
        int y1 = y + 1 + (int)(scrollPercentage * (height - scrollerHeight - 2));
        int w = width - 2;
        int h = scrollerHeight;

        RenderSystem.setShaderTexture(0, UI);

        blit(pPoseStack, x1, y1, 2, 2, startU, startV, 2, 2, AreaRenderer.TEXTURE_WIDTH, AreaRenderer.TEXTURE_HEIGHT); // top left
        blit(pPoseStack, x1, y1 + h - 2, 2, 2, startU, startV + 3, 2, 2, AreaRenderer.TEXTURE_WIDTH, AreaRenderer.TEXTURE_HEIGHT); // bottom left
        blit(pPoseStack, x1 + w - 2, y1, 2, 2, startU + 3, startV, 2, 2, AreaRenderer.TEXTURE_WIDTH, AreaRenderer.TEXTURE_HEIGHT); // top right
        blit(pPoseStack, x1 + w - 2, y1 + h - 2, 2, 2, startU + 3, startV + 3, 2, 2, AreaRenderer.TEXTURE_WIDTH, AreaRenderer.TEXTURE_HEIGHT); // bottom right

        blit(pPoseStack, x1 + 2, y1, w - 4, 2, startU + 2, startV, 1, 2, AreaRenderer.TEXTURE_WIDTH, AreaRenderer.TEXTURE_HEIGHT); // top
        blit(pPoseStack, x1 + 2, y1 + h - 2, w - 4, 2, startU + 2, startV + 3, 1, 2, AreaRenderer.TEXTURE_WIDTH, AreaRenderer.TEXTURE_HEIGHT); // bottom
        blit(pPoseStack, x1, y1 + 2, 2, h - 4, startU, startV + 2, 2, 1, AreaRenderer.TEXTURE_WIDTH, AreaRenderer.TEXTURE_HEIGHT); // left
        blit(pPoseStack, x1 + w - 2, y1 + 2, 2, h - 4, startU + 3, startV + 2, 2, 1, AreaRenderer.TEXTURE_WIDTH, AreaRenderer.TEXTURE_HEIGHT); // right
        
        for (int i = 0; i < h - 4; i += 2) {
            blit(pPoseStack, x1 + 2, y1 + 2 + i, w - 4, i < h - 4 ? 2 : 1, startU + 2, startV + 2, 1, i < h - 4 ? 2 : 1, AreaRenderer.TEXTURE_WIDTH, AreaRenderer.TEXTURE_HEIGHT);
        }
    }

    @Override
    public boolean isInArea(double mouseX, double mouseY) {
        return scrollArea == null || isMouseOver(mouseX, mouseY) || scrollArea.isInBounds(mouseX, mouseY);
    }  
}

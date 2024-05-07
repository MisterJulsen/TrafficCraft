package de.mrjulsen.legacydragonlib.client.gui.widgets;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.legacydragonlib.DragonLibConstants;
import de.mrjulsen.legacydragonlib.client.gui.DynamicGuiRenderer;
import de.mrjulsen.legacydragonlib.client.gui.GuiAreaDefinition;
import de.mrjulsen.legacydragonlib.client.gui.GuiUtils;
import de.mrjulsen.legacydragonlib.client.gui.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.legacydragonlib.client.gui.DynamicGuiRenderer.ButtonState;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorizontalScrollBar extends DragonLibWidgetBase implements IExtendedAreaWidget { 

    public static final int DEFAULT_STEP_SIZE = 1;

    private final GuiAreaDefinition scrollArea;

    private double scrollPercentage;
    private int scroll;
    private int maxScroll = 2;
    private boolean isScrolling = false;

    private int maxColumnsOnPage = 1;
    private int scrollerWidth = 15;
    private int stepSize = DEFAULT_STEP_SIZE;

    private boolean autoScrollerWidth = false;

    // Events
    public Consumer<HorizontalScrollBar> onValueChanged;

    public HorizontalScrollBar(int x, int y, int w, int h, GuiAreaDefinition scrollArea) {
        super(x, y, Math.max(7, w), Math.max(7, h), null);
        this.scrollArea = scrollArea;
    }

    public HorizontalScrollBar(int x, int y, int w, GuiAreaDefinition scrollArea) {
        this(x, y, w, 14, scrollArea);
    }

    public HorizontalScrollBar(int x, int y, int w, int h) {
        this(x, y, w, h, null);
    }

    public HorizontalScrollBar(int x, int y, int w) {
        this(x, y, w, null);
    }

    public HorizontalScrollBar setStepSize(int c) {
        stepSize = Math.max(DEFAULT_STEP_SIZE, c);
        return this;
    }

    public HorizontalScrollBar setMaxColumnsOnPage(int c) {
        maxColumnsOnPage = Math.max(1, c);
        return this;
    }

    public HorizontalScrollBar setAutoScrollerWidth(boolean b) {
        autoScrollerWidth = b;
        return this;
    }

    public HorizontalScrollBar setScrollerWidth(int w) {
        scrollerWidth = Math.max(5, w);
        return this;
    }

    public HorizontalScrollBar updateMaxScroll(int columns) {
        this.maxScroll = Math.max(columns - maxColumnsOnPage, 0);
        if (autoScrollerWidth) {
            this.scrollerWidth = Math.max((int)((width - 2) / Math.max(columns / (float)maxColumnsOnPage, 1.0f)), 5);
        }
        return this;
    }

    public HorizontalScrollBar setOnValueChangedEvent(Consumer<HorizontalScrollBar> event) {
        this.onValueChanged = event;
        return this;
    }

    public boolean getAutoScrollerWidth() {
        return this.autoScrollerWidth;
    }

    public int getScrollValue() {
        return scroll;
    }

    public int getMaxScroll() {
        return maxScroll;
    }

    public int getMaxColumnsOnPage() {
        return maxColumnsOnPage;
    }



    @Override
    public boolean onClick(double pMouseX, double pMouseY) {
        if (isMouseOver(pMouseX, pMouseY) && canScroll()) {
            isScrolling = true;
            scrollTo(pMouseX);
        }
        return true;
    }

    @Override
    protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
        if (this.isScrolling) {
            scrollTo(pMouseX);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (canScroll()) {
            scroll = Mth.clamp((int)(scroll - pDelta * stepSize), 0, maxScroll);

            int i = maxScroll;
            this.scrollPercentage = (double)this.scrollPercentage - pDelta * stepSize / (double)i;
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
        int i = x + 1;
        int j = i + width - 2;

        this.scrollPercentage = (mousePos - (double)i - ((double)scrollerWidth / 2.0D)) / (double)(j - i - scrollerWidth);
        this.scrollPercentage = Mth.clamp(this.scrollPercentage, 0.0F, 1.0F);
        scroll = Math.max(0, (int)Math.round(scrollPercentage * maxScroll));
        
        if (onValueChanged != null)
            onValueChanged.accept(this);
    }

    public void scrollToColumn(int pos) {
        scroll = Mth.clamp(pos, 0, getMaxScroll());
        
        if (onValueChanged != null)
            onValueChanged.accept(this);
    }

    public boolean canScroll() {
        return maxScroll > 0;
    }

    @Override
    public void renderWidget(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        // Render background
        DynamicGuiRenderer.renderArea(pPoseStack, x, y, width, height, AreaStyle.GRAY, ButtonState.SUNKEN);

        // Render scrollbar
        int startU = canScroll() ? 20 : 25;
        int startV = 5;

        int y1 = y + 1;
        int x1 = x + 1 + (int)(scrollPercentage * (width - scrollerWidth - 2));
        int w = scrollerWidth;
        int h = height - 2;

        GuiUtils.blit(DragonLibConstants.UI, pPoseStack, x1, y1, 2, 2, startU, startV, 2, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // top left
        GuiUtils.blit(DragonLibConstants.UI, pPoseStack, x1, y1 + h - 2, 2, 2, startU, startV + 3, 2, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // bottom left
        GuiUtils.blit(DragonLibConstants.UI, pPoseStack, x1 + w - 2, y1, 2, 2, startU + 3, startV, 2, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // top right
        GuiUtils.blit(DragonLibConstants.UI, pPoseStack, x1 + w - 2, y1 + h - 2, 2, 2, startU + 3, startV + 3, 2, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // bottom right

        GuiUtils.blit(DragonLibConstants.UI, pPoseStack, x1 + 2, y1, w - 4, 2, startU + 2, startV, 1, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // top
        GuiUtils.blit(DragonLibConstants.UI, pPoseStack, x1 + 2, y1 + h - 2, w - 4, 2, startU + 2, startV + 3, 1, 2, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // bottom
        GuiUtils.blit(DragonLibConstants.UI, pPoseStack, x1, y1 + 2, 2, h - 4, startU, startV + 2, 2, 1, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // left
        GuiUtils.blit(DragonLibConstants.UI, pPoseStack, x1 + w - 2, y1 + 2, 2, h - 4, startU + 3, startV + 2, 2, 1, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT); // right
        
        for (int i = 0; i < w - 4; i += 2) {
            GuiUtils.blit(DragonLibConstants.UI, pPoseStack, x1 + 2 + i, y1 + 2, i < w - 4 ? 2 : 1, h - 4, startU + 2, startV + 2, i < w - 4 ? 2 : 1, 1, DynamicGuiRenderer.TEXTURE_WIDTH, DynamicGuiRenderer.TEXTURE_HEIGHT);
        }
    }

    @Override
    public boolean isInArea(double mouseX, double mouseY) {
        return scrollArea == null || isMouseOver(mouseX, mouseY) || scrollArea.isInBounds(mouseX, mouseY);
    }  
}

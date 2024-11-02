package de.mrjulsen.trafficcraft.client.widgets;

import java.util.Map;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLRenderable;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLScrollableWidgetContainer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLVerticalScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLWidgetContainer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.client.screen.TrafficLightScheduleEditor;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.data.TrafficLightScheduleEntryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TrafficLightScheduleContainer extends DLWidgetContainer {
    
    public static final int LEFT_PADDING = 8;
    public static final int SPACING = 8;
    public static final int DEFAULT_ENTRY_HEIGHT = 18;
    public static final int TIMELINE_UW = 9;
    public static final int TIMELINE_VH = 9;
    public static final int TIMELINE_WIDTH = 20;

    public final GuiAreaDefinition area;
    private final TrafficLightScheduleInnerContainer innerContainer;

    public TrafficLightScheduleContainer(TrafficLightSchedule schedule, boolean showIdBox, Map<Integer, TrafficLightType> signalTypes, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.area = new GuiAreaDefinition(x(), y(), width() - 8, height());

        DLVerticalScrollBar scrollBar = addRenderableWidget(new DLVerticalScrollBar(x() + width() - 8, y(), 8, height(), area));
        innerContainer = addRenderableWidget(new TrafficLightScheduleInnerContainer(this, schedule, showIdBox, signalTypes, x() + 1, y() + 1, width() - 8 - 2, height() - 2, scrollBar));
        scrollBar.setAutoScrollerSize(true);
        scrollBar.setScreenSize(height() - 2);
        scrollBar.setStepSize(8);
        scrollBar.withOnValueChanged(bar -> {
            innerContainer.setYScrollOffset(bar.getScrollValue());
        });

        init();
    }
    
    public void init() {
        innerContainer.init();
    }

    public double getScrollOffset() {
        return innerContainer.getYScrollOffset();
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {        
        DynamicGuiRenderer.renderContainerBackground(graphics, area, DynamicGuiRenderer.CONTAINER_BACKGROUND_COLOR);
        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean consumeScrolling(double mouseX, double mouseY) {
        return false;
    }

    private static class TrafficLightScheduleInnerContainer extends DLScrollableWidgetContainer {

        private static final Component textStart = TextUtils.translate("gui.trafficcraft.trafficlightschedule.start");
        private static final Component textEnd = TextUtils.translate("gui.trafficcraft.trafficlightschedule.end");

        public static final int LEFT_PADDING = TrafficLightScheduleContainer.LEFT_PADDING;
        public static final int SPACING = TrafficLightScheduleContainer.SPACING;
        public static final int DEFAULT_ENTRY_HEIGHT = TrafficLightScheduleContainer.DEFAULT_ENTRY_HEIGHT;
        public static final int TIMELINE_UW = TrafficLightScheduleContainer.TIMELINE_UW;
        public static final int TIMELINE_VH = TrafficLightScheduleContainer.TIMELINE_VH;
        public static final int TIMELINE_WIDTH = TrafficLightScheduleContainer.TIMELINE_WIDTH;

        private final TrafficLightSchedule schedule;
        private final DLVerticalScrollBar scrollBar;
        private final Map<Integer, TrafficLightType> signalTypes;
        private final boolean showIdBox;
        private final TrafficLightScheduleContainer parent;

        private int currentY;

        public TrafficLightScheduleInnerContainer(TrafficLightScheduleContainer parent, TrafficLightSchedule schedule, boolean showIdBox, Map<Integer, TrafficLightType> signalTypes, int x, int y, int width, int height, DLVerticalScrollBar scrollBar) {
            super(x, y, width, height);
            this.schedule = schedule;
            this.scrollBar = scrollBar;
            this.signalTypes = signalTypes;
            this.showIdBox = showIdBox;
            this.parent = parent;
            init();
        }

        @Override
        public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
            GuiUtils.drawTexture(
                TrafficLightScheduleEditor.WIDGETS,
                graphics,
                x() + TrafficLightScheduleEditor.ENTRY_PADDING + TIMELINE_WIDTH / 2 - TrafficLightScheduleEditor.TIMELINE_UW / 2,
                y(),
                TrafficLightScheduleEditor.TIMELINE_UW,
                height(),
                27,
                20,
                TrafficLightScheduleEditor.TIMELINE_UW,
                1,
                TrafficLightScheduleEditor.TEXTURE_WIDTH,
                TrafficLightScheduleEditor.TEXTURE_HEIGHT
            );

            super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
            GuiUtils.fillGradient(graphics, x(), y(), 0, width(), 10, 0x77000000, 0x00000000);
            GuiUtils.fillGradient(graphics, x(), y() + height() - 10, 0, width(), 10, 0x00000000, 0x77000000);
        }

        public void init() {
            clearWidgets();
            currentY = 1;
            currentY += addRenderableOnly(new TrafficLightScheduleTextEntry(x(), y() + currentY, textStart)).height();
            for (TrafficLightScheduleEntryData entry : schedule.getEntries()) {
                final TrafficLightScheduleEntryData dataEntry = entry;
                currentY += addRenderableWidget(new TrafficLightScheduleEntry(parent, signalTypes, !showIdBox, entry, x(), y() + currentY, width(), (data) -> {
                    schedule.getEntries().removeIf(x -> x == dataEntry);
                    init();
                }, (e, offset) -> {
                    int len = schedule.getEntries().size();
                    int idx = schedule.getEntries().indexOf(dataEntry);
                    if (idx < 0) {
                        return;
                    }
                    if (offset > 0 && idx < len - 1) {
                        schedule.getEntries().moveForth(idx, 1);
                    } else if (offset < 0 && idx > 0) {
                        schedule.getEntries().moveBack(idx, 1);
                    }
                    init();
                })).height();
            }
            currentY += addRenderableOnly(new TrafficLightScheduleTextEntry(x(), y() + currentY, textEnd)).height();

            this.scrollBar.updateMaxScroll(currentY);
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.HOVERED;
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {}

        @Override
        public boolean consumeScrolling(double mouseX, double mouseY) {
            return false;
        }
    }

    private static final class TrafficLightScheduleTextEntry extends DLRenderable {

        private final Font font = Minecraft.getInstance().font;

        private static final ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath(TrafficCraft.MOD_ID, "textures/gui/traffic_light_schedule_icons.png");
        private static final int TEXTURE_WIDTH = 64;
        private static final int TEXTURE_HEIGHT = 64;

        private static final int SPACING = TrafficLightScheduleInnerContainer.SPACING;
        private static final int LEFT_PADDING = TrafficLightScheduleInnerContainer.LEFT_PADDING;
        private static final int TIMELINE_WIDTH = TrafficLightScheduleInnerContainer.TIMELINE_WIDTH;
        private static final int DEFAULT_ENTRY_HEIGHT = TrafficLightScheduleInnerContainer.DEFAULT_ENTRY_HEIGHT;
        private static final int TIMELINE_UW = TrafficLightScheduleInnerContainer.TIMELINE_UW;
        private static final int TIMELINE_VH = TrafficLightScheduleInnerContainer.TIMELINE_VH;

        private final Component text;
        
        @SuppressWarnings("resource")
        public TrafficLightScheduleTextEntry(int x, int y, Component text) {
            super(x, y, TIMELINE_WIDTH + Minecraft.getInstance().font.width(text) + 8, DEFAULT_ENTRY_HEIGHT + SPACING);
            this.text = text;
        }
        
        @Override
        public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
            DynamicGuiRenderer.renderArea(graphics, x() + LEFT_PADDING, y() + SPACING / 2, width(), DEFAULT_ENTRY_HEIGHT, AreaStyle.GRAY, ButtonState.BUTTON);
            GuiUtils.drawString(graphics, font, x() + LEFT_PADDING + TIMELINE_WIDTH, y() + height() / 2 - font.lineHeight / 2, text, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
            
            GuiUtils.drawTexture(WIDGETS, graphics, x() + LEFT_PADDING + TIMELINE_WIDTH / 2 - TIMELINE_UW / 2, y(), TIMELINE_UW, height(), 27, 20, TIMELINE_UW, TIMELINE_VH, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            GuiUtils.drawTexture(WIDGETS, graphics, x() + LEFT_PADDING + TIMELINE_WIDTH / 2 - TIMELINE_UW / 2, y() + height() / 2 - TIMELINE_VH / 2, TIMELINE_UW, TIMELINE_VH, 0, 20, TIMELINE_UW, TIMELINE_VH, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            
        }
    }
    
}

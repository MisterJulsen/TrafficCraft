package de.mrjulsen.trafficcraft.client.widgets.data;

import java.util.Map;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData.StretchedSprite;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.BorderLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.client.widgets.TrafficLightScheduleEntry;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.data.TrafficLightScheduleEntryData;
import net.minecraft.network.chat.Component;

public class TrafficLightScheduleEditorWidget extends DLGuiComponent {
    
    public static final DLTextureSheet ICONS = new DLTextureSheet(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/gui/traffic_light_schedule_icons.png"));
    public static final String SPRITE_TIMELINE_NODE = "timeline_node";
    public static final String SPRITE_TIMELINE_ACTION = "timeline_action";
    public static final String SPRITE_TIMELINE_DELAY = "timeline_delay";
    public static final String SPRITE_TIMELINE_EDGE = "timeline_edge";    

    public static final int SPRITE_SIZE = 9;
    public static final int PADDING = 8;
    public static final int ENTRY_PADDING_LEFT = 6;
    public static final int ENTRY_PADDING_RIGHT = 8;
    public static final int ENTRY_PADDING_TIMELINE_TEXT = 6;
    public static final int TIMELINE_ICON_X = PADDING + ENTRY_PADDING_LEFT;
    public static final int TEXT_X = PADDING + ENTRY_PADDING_LEFT + SPRITE_SIZE + ENTRY_PADDING_TIMELINE_TEXT;
    
    private final Component textStart = TextUtils.translate("gui.trafficcraft.trafficlightschedule.start");
    private final Component textEnd = TextUtils.translate("gui.trafficcraft.trafficlightschedule.end");

    private final DLScrollBar scrollBar;
    private final DLPanel contentPanel;

    private final TrafficLightSchedule schedule;
    private final boolean showIdBox;
    private final Map<Integer, TrafficLightType> signalTypes;

    public TrafficLightScheduleEditorWidget(int x, int y, int w, int h, TrafficLightSchedule schedule, boolean showIdBox, Map<Integer, TrafficLightType> signalTypes) {
        super(x, y, w, h);
        this.schedule = schedule;
        this.showIdBox = showIdBox;
        this.signalTypes = signalTypes;

        scrollBar = addComponent(new DLScrollBar(0, 0, 8, 0, Orientation.VERTICAL));
        scrollBar.layoutContraint.set(BorderLayout.BorderPosition.EAST);
        scrollBar.scrollerSize.set(0);
        scrollBar.inputConsumptionPolicy.set(c -> true);

        DLPanel contentPanelWrapper = addComponent(new DLPanel(0, 0, 10, 10));
        contentPanelWrapper.layoutContraint.set(BorderLayout.BorderPosition.CENTER);
        contentPanelWrapper.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                ICONS.getSprite("container").render(e.graphics(), 0, 0, s.width(), s.height());
            }
            return false;
        });

        contentPanel = contentPanelWrapper.addComponent(new DLPanel(1, 1, contentPanelWrapper.width() - 2, contentPanelWrapper.height() - 2));
        contentPanel.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);
        contentPanel.anchor.set(EAlign.values());
        contentPanel.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                if (ICONS.getSprite(SPRITE_TIMELINE_EDGE) instanceof StretchedSprite sprite) {
                    sprite.render(e.graphics(), TIMELINE_ICON_X, 0, sprite.width(), s.height());
                }
            }
            return false;
        });
        contentPanelWrapper.addEventListener(DLGuiStandardEvents.ScrollEvent.class, scrollBar::invokeEvent);

        FlowLayout contentLayout = new FlowLayout();
        contentLayout.fillCrossAxis.set(true);
        contentLayout.flowDirection.set(Direction.VERTICAL);
        contentLayout.wrap.set(false);
        contentLayout.padding.set(new Padding(PADDING / 2, 0, PADDING / 2, 0));
        contentPanel.addEventListener(DLGuiStandardEvents.ComponentLayoutUpdatedEvent.class, (s, e) -> {
            scrollBar.screenSize.set(s.height());
            scrollBar.max.set(e.layoutResult().contentHeight());
            return false;
        });
        contentPanel.layout.set(contentLayout);
        scrollBar.addEventListener(DLScrollBar.ValueChangedEvent.class, (s, e) -> {
            contentPanel.setScrollOffsetY(e.value());
            return false;
        });
        refresh();
        
        BorderLayout layout = new BorderLayout(0, 0);
        this.layout.set(layout);

    }

    public void refresh() {
        double scrollValue = scrollBar.value.get();
        contentPanel.clearComponents();
        contentPanel.addComponent(new TrafficLightScheduleTextEntry(0, 0, textStart));
        for (TrafficLightScheduleEntryData entry : schedule.getEntries()) {
            final TrafficLightScheduleEntryData dataEntry = entry;
            contentPanel.addComponent(new TrafficLightScheduleEntry(0, 0, width(), entry, !showIdBox, signalTypes, (data) -> {
                schedule.getEntries().removeIf(a -> a == dataEntry);
                refresh();
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
                refresh();
            })).height();
        }
        contentPanel.addComponent(new TrafficLightScheduleTextEntry(0, 0, textEnd));
        scrollBar.value.set(scrollValue);
    }



    private static final class TrafficLightScheduleTextEntry extends DLGuiComponent {

        private final Component text;
        
        public TrafficLightScheduleTextEntry(int x, int y, Component text) {
            super(x, y, 0, 28);
            this.text = text;
            inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);
        }

        @Override
        public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {            
            DLTextureSheet.DRAGONLIB_UI.getSprite("button_gray_normal").render(graphics, PADDING, PADDING / 2, TEXT_X + graphics.defaultFont().width(text) + ENTRY_PADDING_RIGHT, 20);
            ICONS.getSprite(SPRITE_TIMELINE_EDGE).render(graphics, TIMELINE_ICON_X, 0, SPRITE_SIZE, height());
            ICONS.getSprite(SPRITE_TIMELINE_NODE).render(graphics, TIMELINE_ICON_X, height() / 2 - SPRITE_SIZE / 2, SPRITE_SIZE, SPRITE_SIZE);
            GuiUtils.drawString(graphics, graphics.defaultFont(), TEXT_X, height() / 2 - graphics.defaultFont().lineHeight / 2, text, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);            
        }
    }
    
}

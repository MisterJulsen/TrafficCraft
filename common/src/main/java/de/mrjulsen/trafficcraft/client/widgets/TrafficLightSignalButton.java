package de.mrjulsen.trafficcraft.client.widgets;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.client.widgets.data.TrafficLightScheduleEditorWidget;
import de.mrjulsen.trafficcraft.data.TrafficLightScheduleEntryData;

public class TrafficLightSignalButton extends DLButton {

    private final TrafficLightScheduleEntryData entry;
    private final TrafficLightColor signal;

    private final TrafficLightColor[] relatedSignals;

    public TrafficLightSignalButton(TrafficLightScheduleEntryData entry, TrafficLightColor signal, TrafficLightType type) {
        super(0, 0, 12, 12);
        this.entry = entry;
        this.signal = signal;
        this.relatedSignals = type == null ? signal.getSimilar() : new TrafficLightColor[] { signal };

        cursor.set(CursorType.HAND);

        addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            if (entry.getEnabledColors().contains(signal)) {
                entry.disableColors(List.of(signal));
            } else {
                entry.enableColors(List.of(signal));
            }
            return false;
        });
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.fill(graphics, 0, 0, width(), height(), isSelected() ? DLColor.WHITE : DLColor.fromInt(0xFFA7A7A7));
        TrafficLightScheduleEditorWidget.ICONS.getSprite(relatedSignals[(int)((System.currentTimeMillis() / 1000) % relatedSignals.length)].getName()).render(graphics, 1, 1, width() - 2, height() - 2);        
        if (!entry.getEnabledColors().contains(signal)) {
            GuiUtils.fill(graphics, 1, 1, width() - 2, height() - 2, DLColor.fromInt(0xAA000000));
        }
    }
    
}

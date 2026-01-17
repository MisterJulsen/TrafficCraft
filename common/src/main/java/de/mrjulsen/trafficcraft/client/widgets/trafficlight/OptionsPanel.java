package de.mrjulsen.trafficcraft.client.widgets.trafficlight;

import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

public class OptionsPanel extends DLGuiComponent {

    public OptionsPanel(Direction flowDirection) {
        super(0, 0, 1, 1);
        FlowLayout layout = new FlowLayout();
        layout.padding.set(new Padding(1));
        layout.flowDirection.set(flowDirection);
        layout.wrap.set(false);
        addEventListener(DLGuiStandardEvents.ComponentLayoutUpdatedEvent.class, (s, e) -> {
            setWidth(e.layoutResult().contentWidth());
            setHeight(e.layoutResult().contentHeight());
            return false;
        });
        this.layout.set(layout);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DLTextureSheet.DRAGONLIB_UI.getSprite("slot").render(graphics, 0, 0, width(), height());
    }

}

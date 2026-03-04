package de.mrjulsen.trafficcraft.client.widgets.trafficlight;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.TrafficCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public abstract class AbstractTrafficLightSettings extends DLGuiComponent {

    protected static final DLTexture ARROW_TEXTURE = new DLTexture(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/gui/window_arrow.png"), 32, 32);
    protected static final int ARROW_WIDTH = 12;
    protected static final int ARROW_HEIGHT = 18;

    protected final Component title;
    protected final boolean showArrow;

    public AbstractTrafficLightSettings(int x, int y, int w, int h, Component title, boolean showArrow) {
        super(x, y, w, h);
        this.title = title;
        this.showArrow = showArrow;

        FlowLayout layout = new FlowLayout();
        layout.padding.set(new Padding(20, 7, 7, getArrowOffset() + 7));
        layout.flowDirection.set(Direction.VERTICAL);
        layout.verticalGap.set(5);
        layout.wrap.set(false);
        addEventListener(DLGuiStandardEvents.ComponentLayoutUpdatedEvent.class, (s, e) -> {
            setSize(Math.max(e.layoutResult().contentWidth(), Minecraft.getInstance().font.width(title) + layout.padding.get().left() + layout.padding.get().right()), e.layoutResult().contentHeight());
            return false;
        });
        this.layout.set(layout);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DLTextureSheet.DRAGONLIB_UI.getSprite(DLTextureSheet.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, getArrowOffset(), 0, width() - getArrowOffset(), height());
        GuiUtils.drawTexture(ARROW_TEXTURE, graphics, 0, height() / 2 - ARROW_HEIGHT / 2, ARROW_WIDTH, ARROW_HEIGHT);
        GuiUtils.drawString(graphics, graphics.defaultFont(), getArrowOffset() + 7, 7, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
    }

    protected int getArrowOffset() {
        return showArrow ? 9 : 0;
    }
    
}

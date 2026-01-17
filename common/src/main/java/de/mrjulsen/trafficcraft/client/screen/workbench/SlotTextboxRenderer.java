package de.mrjulsen.trafficcraft.client.screen.workbench;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextEditBox.TextBoxState;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.IStateRenderer;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

public class SlotTextboxRenderer implements IStateRenderer<TextBoxState> {

    public static final SlotTextboxRenderer INSTANCE = new SlotTextboxRenderer();

    @Override
    public void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, TextBoxState state) {
        DLTextureSheet.DRAGONLIB_UI.getSprite("slot").render(graphics, x, y, w, h);
    }
}

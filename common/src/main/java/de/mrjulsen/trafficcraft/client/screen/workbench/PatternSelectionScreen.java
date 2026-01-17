package de.mrjulsen.trafficcraft.client.screen.workbench;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaSimpleButtonRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.client.ModGuiIcons;
import de.mrjulsen.trafficcraft.client.screen.TrafficSignWorkbenchWindow;
import de.mrjulsen.trafficcraft.client.widgets.trafficlight.OptionButton;
import net.minecraft.network.chat.Component;

public class PatternSelectionScreen extends DLGuiComponent {    

    private final Component createPattern = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.createpattern.title");
    private final Component createPatternInstruction = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.createpattern.instruction");

    private TrafficSignShape selectedShape = null;

    public PatternSelectionScreen(TrafficSignWorkbenchWindow win) {
        super(0, 0, win.width(), win.height());
        layoutContraint.set(FlowLayout.FlowConstraint.FILL);
        

        DLButton acceptBtn = new DLButton(width() / 2 - 20, 150, 18, 18);
        acceptBtn.text.set(TextUtils.EMPTY);
        acceptBtn.icon.set(ModGuiIcons.CHECK.getAsSprite(16, 16));
        acceptBtn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_GRAY);
        acceptBtn.enabled.set(false);
        addComponent(acceptBtn);
        acceptBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            win.clearComponents();
            win.addComponent(new EditorScreen(win, selectedShape, null, null, -1));
            return false;
        });

        DLButton declineBtn = new DLButton(width() / 2 + 2, 150, 18, 18);
        declineBtn.text.set(TextUtils.EMPTY);
        declineBtn.icon.set(ModGuiIcons.CANCEL.getAsSprite(16, 16));
        declineBtn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_GRAY);
        addComponent(declineBtn);
        declineBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            win.clearComponents();
            win.addComponent(new MainScreen(win));
            return false;
        });
        
        int panelWidth = 2 + 18 * 4;
        OptionsPanel patternPanel = new OptionsPanel(width() / 2 - panelWidth / 2, 80, panelWidth);
        for (TrafficSignShape shape : TrafficSignShape.values()) {
            final TrafficSignShape fShape = shape;
            OptionButton btn = new OptionButton(new DLSprite(new DLTexture(shape.getIconResourceLocation(), 16, 16), 16, 16));
            patternPanel.addComponent(btn);
            btn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                this.selectedShape = fShape;
                acceptBtn.enabled.set(selectedShape != null);
                return false;
            });
        }
        addComponent(patternPanel);



    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, 40, createPattern, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.CENTER, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, 60, createPatternInstruction, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.CENTER, false);
    }

    private static class OptionsPanel extends DLGuiComponent {

        public OptionsPanel(int x, int y, int w) {
            super(x, y, w, 1);
            FlowLayout layout = new FlowLayout();
            layout.padding.set(new Padding(1));
            layout.flowDirection.set(FlowLayout.Direction.HORIZONTAL);
            layout.wrap.set(true);
            addEventListener(DLGuiStandardEvents.ComponentLayoutUpdatedEvent.class, (s, e) -> {
                setHeight(e.layoutResult().contentHeight());
                return false;
            });
            this.layout.set(layout);
        }

        @Override
        public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
            DLTextureSheet.DRAGONLIB_UI.getSprite("button_brown_down").render(graphics, 0, 0, width(), height());
        }

    }
    
}

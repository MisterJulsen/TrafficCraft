package de.mrjulsen.trafficcraft.client.screen;

import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils.TextureFillMode;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.client.screen.workbench.MainScreen;
import net.minecraft.client.Minecraft;

public class TrafficSignWorkbenchWindow extends DLWindow {

    private static final DLTexture OVERLAY = new DLTexture(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/gui/traffic_sign_workbench_overlay.png"), 256, 256);

    private final TrafficSignWorkbenchMenu menu;
    private boolean active;

    public TrafficSignWorkbenchWindow(DLWindowManager manager, TrafficSignWorkbenchMenu menu) {
        super(manager);
        this.menu = menu;        
        //inputConsumptionPolicy.set(c -> false);

        FlowLayout layout = new FlowLayout();
        layout.fillCrossAxis.set(true);
        this.layout.set(layout);
    }

    public TrafficSignWorkbenchMenu getMenu() {
        return menu;
    }

    @Override
    public void close() throws Exception {
        Minecraft.getInstance().player.closeContainer();
        super.close();
    }

    @Override
    public void tick() {
        super.tick();
        boolean hasItem = !menu.patternSlot.getItem().isEmpty();
        boolean changed = active != hasItem;
        
        if (changed) {
            active = hasItem;
            if (hasItem) {
                addComponent(new MainScreen(this));
            } else {
                clearComponents();
            }
        }
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        if (!active) {
            return;
        }
        GuiUtils.drawTexture(OVERLAY, graphics, 36, 14, 158, 174, 0, 0, 158, 174, TextureFillMode.STRETCH);
    }
    
}

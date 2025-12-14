package de.mrjulsen.trafficcraft.client.screen;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLContainerScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils.TextureFillMode;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TrafficSignWorkbenchGui extends DLContainerScreen<TrafficSignWorkbenchMenu> {

    public static final Component title = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.title");

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int WIDTH = 230;
    private static final int HEIGHT = 256;


    private int guiLeft;
    private int guiTop;
    private static final ResourceLocation GUI = new ResourceLocation(TrafficCraft.MOD_ID, "textures/gui/traffic_sign_workbench.png");

    public TrafficSignWorkbenchGui(TrafficSignWorkbenchMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, mgr -> new TrafficSignWorkbenchWindow(mgr, pMenu));
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelY = 188;
        this.inventoryLabelX = 6;
    }

    @Override
    public void init() {
        super.init();
        guiLeft = this.width / 2 - WIDTH / 2;
        guiTop = this.height / 2 - HEIGHT / 2;

        DLWindow window = getWindowManager().getCurrentActiveWindow();
        if (window != null) {
            window.setPosition(guiLeft, guiTop);
            window.setSize(WIDTH, 197);
        }
    }

    @Override
    protected void renderBg(DLGuiGraphics guiGraphics, int mouseX, int mouseY) {
        GuiUtils.drawTexture(GUI, guiGraphics, guiLeft, guiTop, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, TextureFillMode.STRETCH, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        renderTooltip(guiGraphics.graphics(), mouseX, mouseY);
    }
    


    protected enum TrafficSignWorkbenchMode {
        EMPTY,
        DEFAULT,
        CREATE_NEW,
        EDITOR
    }

    protected enum TrafficSignWorkbenchEditorTool {
        DRAW(0),
        ERASER(1),
        PICK_COLOR(2),
        FILL(3);

        private int index;

        private TrafficSignWorkbenchEditorTool(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        public static TrafficSignWorkbenchEditorTool byIndex(int index) {
            for (TrafficSignWorkbenchEditorTool shape : TrafficSignWorkbenchEditorTool.values()) {
                if (shape.getIndex() == index) {
                    return shape;
                }
            }
            return TrafficSignWorkbenchEditorTool.DRAW;
        }
    }

    
}

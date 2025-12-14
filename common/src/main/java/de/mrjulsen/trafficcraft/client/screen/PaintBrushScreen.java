package de.mrjulsen.trafficcraft.client.screen;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLToggleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaSimpleButtonRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils.TextureFillMode;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.network.packets.cts.PaintBrushPacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import net.minecraft.network.chat.Component;

public class PaintBrushScreen extends DLWindow {

    public static final Component title = TextUtils.translate("gui.trafficcraft.paint_brush.title");
    public static final Component titleOpenFileDialog = TextUtils.translate("gui.trafficcraft.signpicker.openfiledialog");
    public static final Component btnDoneText = TextUtils.translate("gui.trafficcraft.signpicker.load");
    public static final Component tooltipImport = TextUtils.translate("gui.trafficcraft.signpicker.tooltip.import");

    private static final int WIDTH = 187;
    private static final int HEIGHT = 171;
    private static final int MAX_ENTRIES_IN_ROW = 9;
    private static final int MAX_ROWS = 6;
    private static final int ICON_BUTTON_WIDTH = 18;
    private static final int ICON_BUTTON_HEIGHT = 18;
      
    private DLTexture preview;
    
    private final int paint;
    private final PaintColor color;
    private final DLColor diffuseColor;
    private int patternId;

    private final DLPanel groupPatterns;
    private final DLScrollBar scrollbar;

    private DLTexture[] resources;
    private int count;

    public PaintBrushScreen(DLWindowManager manager, int patternId, int paint, PaintColor color) {
        super(manager);
        setSize(WIDTH, HEIGHT);
        windowSpawnPosition.set(WindowPosition.CENTER);

        this.patternId = patternId;
        this.paint = paint;
        this.color = color;
        this.diffuseColor = color.getTextureColor();

        DLTexture path = new DLTexture(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/block/sign_blank.png"), 32, 32);
        List<DLTexture> locs = new ArrayList<>();

        for (int i = 1; i <= Constants.MAX_ASPHALT_PATTERNS + 1; i++) {
            locs.add(path);
            path = new DLTexture(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/block/patterns/" + i + ".png"), 32, 32);
        }
        this.resources = locs.toArray(DLTexture[]::new);
        this.count = this.resources.length;


        groupPatterns = addComponent(new DLPanel(7, 16, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_WIDTH * MAX_ROWS + 2));
        groupPatterns.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);
        groupPatterns.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                DefaultGuiTextures.DRAGONLIB_UI.getSprite("button_brown_down").render(e.graphics(), 0, 0, s.width(), s.height());
            }
            return false;
        });
        DLPanel innerPanel = groupPatterns.addComponent(new DLPanel(1, 1, groupPatterns.width() - 2, groupPatterns.height() - 2));
        FlowLayout layout = new FlowLayout();
        layout.flowDirection.set(Direction.HORIZONTAL);
        layout.wrap.set(true);        
        innerPanel.layout.set(layout);
        innerPanel.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);
        
        for (int i = 0; i < count; i++) {
            final int j = i;
            DLSprite sprite = new DLSprite(resources[j], ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2, 0, 0, 32, 32);   
            DLToggleButton btnImport = new DLToggleButton(0, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);

            btnImport.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
            btnImport.radioButtonMode.set(true);
            btnImport.text.set(TextUtils.EMPTY);
            btnImport.icon.set(sprite);
            btnImport.iconAlignment.set(ETextAlignment.CENTER);
            btnImport.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);
            btnImport.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                this.preview = resources[j];
                this.patternId = j;
                return false;
            });

            if (patternId == j) {
                btnImport.checked.set(true);
                preview = resources[j];
            }
            innerPanel.addComponent(btnImport);
        }        

        this.scrollbar = addComponent(new DLScrollBar(groupPatterns.x() + groupPatterns.width(), groupPatterns.y(), 8, groupPatterns.height(), Orientation.VERTICAL));
        scrollbar.anchor.set2(EAlign.BOTTOM, EAlign.TOP, EAlign.RIGHT);
        scrollbar.scrollerSize.set(0);
        scrollbar.screenSize.set(innerPanel.height());
        scrollbar.max.set((int)Math.ceil(count / MAX_ENTRIES_IN_ROW * ICON_BUTTON_HEIGHT));
        scrollbar.inputConsumptionPolicy.set(c -> true);
        scrollbar.scrollSteps.set(ICON_BUTTON_HEIGHT);
        scrollbar.addEventListener(DLScrollBar.ValueChangedEvent.class, (s, e) -> {
            innerPanel.setScrollOffsetY(e.value());
            return false;
        });
        addEventListener(DLGuiStandardEvents.ScrollEvent.class, scrollbar::invokeEvent);
    }

    @Override
    public void close() {
        ModNetworkManager.UPDATE_PAINT_BRUSH.send(NetworkDirection.toServer(), new PaintBrushPacket(patternId));
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DefaultGuiTextures.DRAGONLIB_UI.getSprite(DefaultGuiTextures.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());                
        GuiUtils.drawString(graphics, graphics.defaultFont(), WIDTH / 2, 6, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.CENTER, false);
        
        if (preview != null) {
            GuiUtils.setTint(diffuseColor);
            GuiUtils.drawTexture(preview, graphics, 8, 130, 32, 32, 0, 0, 32, 32, TextureFillMode.STRETCH);
            GuiUtils.resetTint();        
        }

        Component textPattern = TextUtils.translate("item.trafficcraft.paint_brush.tooltip.pattern", patternId);
        Component textColor = TextUtils.translate("item.trafficcraft.paint_brush.tooltip.color", color.getValueTranslation().getString());
        Component textPaint = TextUtils.translate("item.trafficcraft.paint_brush.tooltip.paint", (int)(100.0f / Constants.MAX_PAINT * paint));

        GuiUtils.drawString(graphics, graphics.defaultFont(), WIDTH - 7, 130, textPattern, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.RIGHT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), WIDTH - 7, 130 + graphics.defaultFont().lineHeight, textColor, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.RIGHT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), WIDTH - 7, 130 + graphics.defaultFont().lineHeight * 2, textPaint, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.RIGHT, false);
    }
}

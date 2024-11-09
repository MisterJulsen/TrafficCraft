package de.mrjulsen.trafficcraft.client.screen;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLIconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLVerticalScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLAbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.WidgetsCollection;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.net.DLNetworkManager;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.network.packets.cts.PaintBrushPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PaintBrushScreen extends DLScreen {

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
      
    private int guiLeft;
    private int guiTop;
    private ResourceLocation preview;
    private double scroll;
    
    private final int paint;
    private final PaintColor color;
    private final int diffuseColor;
    private int patternId;

    private final WidgetsCollection groupPatterns = new WidgetsCollection();
    private DLVerticalScrollBar scrollbar;
    private boolean updateScrollableContent = true;

    private final ResourceLocation[] resources;
    private final int count;

    public PaintBrushScreen(int patternId, int paint, PaintColor color) {
        super(title);

        this.patternId = patternId;
        this.paint = paint;
        this.color = color;
        this.diffuseColor = color.getTextureColor();

        ResourceLocation path = ResourceLocation.fromNamespaceAndPath(TrafficCraft.MOD_ID, "textures/block/sign_blank.png");
        List<ResourceLocation> locs = new ArrayList<>();

        for (int i = 1; i <= Constants.MAX_ASPHALT_PATTERNS + 1; i++) {
            locs.add(path);
            path = ResourceLocation.fromNamespaceAndPath(TrafficCraft.MOD_ID, "textures/block/patterns/" + i + ".png");
        }
        this.resources = locs.toArray(ResourceLocation[]::new);
        this.count = this.resources.length;
    }

    @Override
    public void onClose() {
        DLNetworkManager.sendToServer(new PaintBrushPacket(patternId));
        super.onClose();
    }

    @Override
    public void init() {
        super.init();
        guiLeft = this.width / 2 - WIDTH / 2;
        guiTop = this.height / 2 - (HEIGHT + 24) / 2; 

        groupPatterns.components.clear();
        
        for (int i = 0; i < count; i++) {
            final int j = i;
            Sprite sprite = new Sprite(resources[j], 32, 32, 0, 0, 32, 32, ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2);
            DLIconButton btnImport = new DLIconButton(ButtonType.RADIO_BUTTON, AreaStyle.BROWN, sprite, groupPatterns, guiLeft + 9, guiTop + 36 + j * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, TextUtils.empty(), (button) -> {
                preview = resources[j];
                patternId = j;
            }) {
                @Override
                public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
                    GuiUtils.setTint(diffuseColor);
                    super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
                    GuiUtils.resetTint();
                }
            }.withAlignment(EAlignment.CENTER);

            if (patternId == j) {
                btnImport.select();
                preview = resources[j];                
            }
            this.addRenderableWidget(btnImport);
        }        

        this.scrollbar = this.addRenderableWidget(new DLVerticalScrollBar(guiLeft + 171, guiTop + 16, 8, ICON_BUTTON_HEIGHT * MAX_ROWS + 2, new GuiAreaDefinition(guiLeft + 7, guiTop + 16, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_HEIGHT * MAX_ROWS + 2))
            .withOnValueChanged(v -> {
                this.scroll = v.getScrollValue();
                if (updateScrollableContent)
                    fillButtons(groupPatterns.components.toArray(DLIconButton[]::new), this.scroll, guiLeft + 8, guiTop + 17, scrollbar);

                updateScrollableContent = true;
            })
            .setAutoScrollerSize(true));

        fillButtons(groupPatterns.components.toArray(DLIconButton[]::new), this.scroll, guiLeft + 8, guiTop + 17, scrollbar);
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderScreenBackground(graphics);
        DynamicGuiRenderer.renderWindow(graphics, guiLeft, guiTop, WIDTH, HEIGHT);
        DynamicGuiRenderer.renderArea(graphics, guiLeft + 7, guiTop + 16, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_HEIGHT * MAX_ROWS + 2, AreaStyle.BROWN, ButtonState.DISABLED);
        
        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
        
        GuiUtils.drawString(graphics, font, guiLeft + WIDTH / 2, guiTop + 6, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.CENTER, false);
        
        if (preview != null) {
            GuiUtils.setTint(diffuseColor);
            GuiUtils.drawTexture(preview, graphics, guiLeft + 8, guiTop + 130, 32, 32, 0, 0, 32, 32, 32, 32);
            GuiUtils.resetTint();        
        }

        Component textPattern = TextUtils.translate("item.trafficcraft.paint_brush.tooltip.pattern", patternId);
        Component textColor = TextUtils.translate("item.trafficcraft.paint_brush.tooltip.color", TextUtils.translate(color.getValueTranslationKey(TrafficCraft.MOD_ID)).getString());
        Component textPaint = TextUtils.translate("item.trafficcraft.paint_brush.tooltip.paint", (int)(100.0f / Constants.MAX_PAINT * paint));

        GuiUtils.drawString(graphics, font, guiLeft + WIDTH - 7, guiTop + 130, textPattern, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.RIGHT, false);
        GuiUtils.drawString(graphics, font, guiLeft + WIDTH - 7, guiTop + 130 + font.lineHeight, textColor, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.RIGHT, false);
        GuiUtils.drawString(graphics, font, guiLeft + WIDTH - 7, guiTop + 130 + font.lineHeight * 2, textPaint, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.RIGHT, false);
    }

    private void fillButtons(DLIconButton[] buttons, double scrollRow, int defX, int defY, DLVerticalScrollBar scrollbar) {
        if (buttons.length <= 0) {
            return;
        }

        int currentRow = -1;
        for (int i = 0; i < buttons.length; i++) {
            if (i % MAX_ENTRIES_IN_ROW == 0)
                currentRow++;

            buttons[i].set_x(defX + (i % MAX_ENTRIES_IN_ROW) * ICON_BUTTON_WIDTH);
            buttons[i].set_y((int)(defY + (currentRow) * ICON_BUTTON_HEIGHT - (scrollRow * ICON_BUTTON_HEIGHT)));
            buttons[i].set_visible(currentRow >= scrollRow && currentRow < scrollRow + MAX_ROWS);
        }

        if (scrollbar != null) {
            updateScrollableContent = false;
            scrollbar.setScreenSize(MAX_ROWS).setMaxScroll(currentRow + 1);
        }
    }
}

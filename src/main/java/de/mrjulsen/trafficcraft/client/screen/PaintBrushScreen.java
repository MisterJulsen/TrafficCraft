package de.mrjulsen.trafficcraft.client.screen;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.DragonLibConstants;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.gui.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.Sprite;
import de.mrjulsen.mcdragonlib.client.gui.WidgetsCollection;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.VerticalScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.AbstractImageButton.Alignment;
import de.mrjulsen.mcdragonlib.client.gui.widgets.AbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.gui.wrapper.CommonScreen;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.cts.PaintBrushPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PaintBrushScreen extends CommonScreen {

    public static final Component title = GuiUtils.translate("gui.trafficcraft.paint_brush.title");
    public static final Component titleOpenFileDialog = GuiUtils.translate("gui.trafficcraft.signpicker.openfiledialog");
    public static final Component btnDoneText = GuiUtils.translate("gui.trafficcraft.signpicker.load");
    public static final Component tooltipImport = GuiUtils.translate("gui.trafficcraft.signpicker.tooltip.import");

    private static final int WIDTH = 187;
    private static final int HEIGHT = 171;
    private static final int MAX_ENTRIES_IN_ROW = 9;
    private static final int MAX_ROWS = 6;
    private static final int ICON_BUTTON_WIDTH = 18;
    private static final int ICON_BUTTON_HEIGHT = 18;
      
    private int guiLeft;
    private int guiTop;
    private ResourceLocation preview;
    private int scroll;
    
    private final int paint;
    private final PaintColor color;
    private final float[] diffuseColor;
    private int patternId;

    private final WidgetsCollection groupPatterns = new WidgetsCollection();
    private VerticalScrollBar scrollbar;

    private final ResourceLocation[] resources;
    private final int count;

    public PaintBrushScreen(int patternId, int paint, PaintColor color) {
        super(title);

        this.patternId = patternId;
        this.paint = paint;
        this.color = color;
        this.diffuseColor = color.getTextureDiffuseColors();

        ResourceLocation path = new ResourceLocation(ModMain.MOD_ID, "textures/block/sign_blank.png");
        List<ResourceLocation> locs = new ArrayList<>();

        for (int i = 1; i <= Constants.MAX_ASPHALT_PATTERNS + 1; i++) {
            locs.add(path);
            path = new ResourceLocation(ModMain.MOD_ID, "textures/block/patterns/" + i + ".png");
        }
        this.resources = locs.toArray(ResourceLocation[]::new);
        this.count = this.resources.length;
    }

    @Override
    public void onClose() {
        NetworkManager.getInstance().send(new PaintBrushPacket(patternId), null);
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
            IconButton btnImport = new IconButton(ButtonType.RADIO_BUTTON, AreaStyle.BROWN, sprite, groupPatterns, guiLeft + 9, guiTop + 36 + j * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, null, (button) -> {
                preview = resources[j];
                patternId = j;
            }) {
                public void renderImage(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
                    GuiUtils.setShaderColor(diffuseColor[0], diffuseColor[1], diffuseColor[2], 1);
                    super.renderImage(pPoseStack, pMouseX, pMouseY, pPartialTick);
                    GuiUtils.setShaderColor(1, 1, 1, 1);
                };
            }.withAlignment(Alignment.CENTER);

            if (patternId == j) {
                btnImport.select();
                preview = resources[j];                
            }
            this.addRenderableWidget(btnImport);
        }        

        this.scrollbar = this.addRenderableWidget(new VerticalScrollBar(guiLeft + 171, guiTop + 16, 8, ICON_BUTTON_HEIGHT * MAX_ROWS + 2, new GuiAreaDefinition(guiLeft + 7, guiTop + 16, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_HEIGHT * MAX_ROWS + 2)).setOnValueChangedEvent(v -> {
            this.scroll = v.getScrollValue();
            fillButtons(groupPatterns.components.toArray(IconButton[]::new), this.scroll, guiLeft + 8, guiTop + 17, scrollbar);
        }).setAutoScrollerHeight(true));

        fillButtons(groupPatterns.components.toArray(IconButton[]::new), this.scroll, guiLeft + 8, guiTop + 17, scrollbar);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack, 0);
        DynamicGuiRenderer.renderWindow(stack, guiLeft, guiTop, WIDTH, HEIGHT);
        DynamicGuiRenderer.renderArea(stack, guiLeft + 7, guiTop + 16, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_HEIGHT * MAX_ROWS + 2, AreaStyle.BROWN, ButtonState.SUNKEN);
        
        this.font.draw(stack, title, guiLeft + WIDTH / 2 - font.width(title) / 2, guiTop + 6, 4210752);

        super.render(stack, mouseX, mouseY, partialTicks);
        
        if (preview != null) {
            GuiUtils.setShaderColor(diffuseColor[0], diffuseColor[1], diffuseColor[2], 1);
            GuiUtils.blit(preview, stack, guiLeft + 8, guiTop + 130, 32, 32, 0, 0, 32, 32, 32, 32);
            GuiUtils.setShaderColor(1, 1, 1, 1);            
        }

        Component textPattern = GuiUtils.translate("item.trafficcraft.paint_brush.tooltip.pattern", patternId);
        Component textColor = GuiUtils.translate("item.trafficcraft.paint_brush.tooltip.color", GuiUtils.translate(color.getTranslatableString()).getString());
        Component textPaint = GuiUtils.translate("item.trafficcraft.paint_brush.tooltip.paint", (int)(100.0f / Constants.MAX_PAINT * paint));

        font.draw(stack, textPattern, guiLeft + WIDTH - 7 - font.width(textPattern), guiTop + 130, DragonLibConstants.DEFAULT_UI_FONT_COLOR);
        font.draw(stack, textColor, guiLeft + WIDTH - 7 - font.width(textColor), guiTop + 130 + font.lineHeight, DragonLibConstants.DEFAULT_UI_FONT_COLOR);
        font.draw(stack, textPaint, guiLeft + WIDTH - 7 - font.width(textPaint), guiTop + 130 + font.lineHeight * 2, DragonLibConstants.DEFAULT_UI_FONT_COLOR);
    }

    private void fillButtons(IconButton[] buttons, int scrollRow, int defX, int defY, VerticalScrollBar scrollbar) {
        if (buttons.length <= 0) {
            return;
        }

        int currentRow = -1;
        for (int i = 0; i < buttons.length; i++) {
            if (i % MAX_ENTRIES_IN_ROW == 0)
                currentRow++;

            buttons[i].x = defX + (i % MAX_ENTRIES_IN_ROW) * ICON_BUTTON_WIDTH;
            buttons[i].y = defY + (currentRow) * ICON_BUTTON_HEIGHT - (scrollRow * ICON_BUTTON_HEIGHT);
            buttons[i].visible = currentRow >= scrollRow && currentRow < scrollRow + MAX_ROWS;
        }

        if (scrollbar != null) {
            scrollbar.setMaxRowsOnPage(MAX_ROWS).updateMaxScroll(currentRow + 1);
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.shouldCloseOnEsc() && pKeyCode == 256 || this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }
}

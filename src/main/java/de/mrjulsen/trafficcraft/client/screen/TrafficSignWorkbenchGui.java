package de.mrjulsen.trafficcraft.client.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.gui.ColorPickerScreen;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.gui.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.Sprite;
import de.mrjulsen.mcdragonlib.client.gui.Tooltip;
import de.mrjulsen.mcdragonlib.client.gui.WidgetsCollection;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.VerticalScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.AbstractImageButton.Alignment;
import de.mrjulsen.mcdragonlib.client.gui.widgets.AbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.utils.ClientTools;
import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.client.TrafficSignTextureCacheClient;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.data.TrafficSignData;
import de.mrjulsen.trafficcraft.item.ColorPaletteItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.cts.ColorPaletteItemPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.PatternCatalogueDeletePacket;
import de.mrjulsen.trafficcraft.network.packets.cts.PatternCatalogueIndexPacketGui;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficSignPatternPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrafficSignWorkbenchGui extends AbstractContainerScreen<TrafficSignWorkbenchMenu> {

    public static final Component title = Utils.translate("gui.trafficcraft.trafficsignworkbench.title");

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int WIDTH = 230;
    private static final int HEIGHT = 256;

    private static final int MAX_ENTRIES_IN_ROW = 4;
    private static final int MAX_ROWS = 3;
    
    private static final int ICON_BUTTON_WIDTH = 18;
    private static final int ICON_BUTTON_HEIGHT = 18;

    private static final int MAX_TOOLBAR1_BUTTONS = 4;


    // Groups
    private final WidgetsCollection groupDefaultModeButtons = new WidgetsCollection();
    private final WidgetsCollection groupEditor = new WidgetsCollection();
    private final WidgetsCollection groupEditorToolbar1 = new WidgetsCollection();
    private final WidgetsCollection groupShapes = new WidgetsCollection();
    private final WidgetsCollection groupCreatePattern = new WidgetsCollection();
    private final WidgetsCollection groupColors = new WidgetsCollection();
    
    // gui
    private int guiLeft;
    private int guiTop;
    private TrafficSignWorkbenchMode mode = TrafficSignWorkbenchMode.EMPTY; 
    private GuiAreaDefinition editorArea, nextButton, prevButton;
    private TrafficSignData preview;
    private EditBox nameBox;
    private IconButton createNewAcceptBtn;

    // tooltips
    private final Map<TrafficSignWorkbenchMode, List<Tooltip>> tooltips = new HashMap<>();

    // data
    private TrafficSignShape shape;
    private int[][] pixels; // image
    private String name;
    private TrafficSignWorkbenchEditorTool tool = TrafficSignWorkbenchEditorTool.DRAW;
    private int selectedColor = 0xFF000000;
    private int selectedIndex = -1;

    // texts
    private final Component createPattern = Utils.translate("gui.trafficcraft.trafficsignworkbench.createpattern.title");
    private final Component createPatternInstruction = Utils.translate("gui.trafficcraft.trafficsignworkbench.createpattern.instruction");
    private final Component emptyPattern = Utils.translate("gui.trafficcraft.trafficsignworkbench.menu.no_pattern");

    private final Component tooltipDefaultNew = Utils.translate("gui.trafficcraft.trafficsignworkbench.menu.add");
    //private final Component tooltipDefaultNewFull1 = Utils.translate("gui.trafficcraft.trafficsignworkbench.menu.add_full1");
    //private final Component tooltipDefaultNewFull2 = Utils.translate("gui.trafficcraft.trafficsignworkbench.menu.add_full2");
    private final Component tooltipDefaultEdit = Utils.translate("gui.trafficcraft.trafficsignworkbench.menu.edit");
    private final Component tooltipDefaultDelete = Utils.translate("gui.trafficcraft.trafficsignworkbench.menu.delete");

    private final Component tooltipEditorToolbarDraw = Utils.translate("gui.trafficcraft.trafficsignworkbench.editor.draw");
    private final Component tooltipEditorToolbarErase = Utils.translate("gui.trafficcraft.trafficsignworkbench.editor.erase");
    private final Component tooltipEditorToolbarPickColor = Utils.translate("gui.trafficcraft.trafficsignworkbench.editor.pick_color");
    private final Component tooltipEditorToolbarFill = Utils.translate("gui.trafficcraft.trafficsignworkbench.editor.fill");
    private final Component tooltipEditorToolbarText = Utils.translate("gui.trafficcraft.trafficsignworkbench.editor.text");
    private final Component tooltipEditorToolbarLoad = Utils.translate("gui.trafficcraft.trafficsignworkbench.editor.load");
    private final Component tooltipEditorToolbarSave = Utils.translate("gui.trafficcraft.trafficsignworkbench.editor.save");
    private final Component tooltipEditorToolbarDiscard = Utils.translate("gui.trafficcraft.trafficsignworkbench.editor.discard");


    // gui textures
    private static final ResourceLocation GUI = new ResourceLocation(ModMain.MOD_ID, "textures/gui/traffic_sign_workbench.png");
    private static final ResourceLocation OVERLAY = new ResourceLocation(ModMain.MOD_ID, "textures/gui/traffic_sign_workbench_overlay.png");

    public TrafficSignWorkbenchGui(TrafficSignWorkbenchMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelY = 188;
        this.inventoryLabelX = 6;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (preview != null) {
            this.preview.close();
        }

        super.onClose();
    }

    @Override
    public void init() {
        super.init();
        guiLeft = this.width / 2 - WIDTH / 2;
        guiTop = this.height / 2 - HEIGHT / 2;

        groupDefaultModeButtons.components.clear();
        groupShapes.components.clear();
        groupCreatePattern.components.clear();
        groupEditor.components.clear();
        groupEditorToolbar1.components.clear();
        groupColors.components.clear();
        tooltips.clear();
        Arrays.stream(TrafficSignWorkbenchMode.values()).forEach(x -> this.tooltips.put(x, new ArrayList<>()));

        //#region DEFAULT MODE CONTROLS

        // Add new
        final IconButton btnNew = this.addRenderableWidget(new IconButton(
            ButtonType.DEFAULT,
            AreaStyle.BROWN,
            ButtonIcons.ADD.getSprite(),
            groupDefaultModeButtons,
            guiLeft + 9,
            guiTop + 36 + 0 * ICON_BUTTON_HEIGHT,
            ICON_BUTTON_WIDTH,
            ICON_BUTTON_HEIGHT,
            null,
            (btn) -> {
                switchMode(TrafficSignWorkbenchMode.CREATE_NEW);
            }
        )).withAlignment(Alignment.CENTER);
        this.tooltips.get(TrafficSignWorkbenchMode.DEFAULT).add(Tooltip.of(tooltipDefaultNew).assignedTo(btnNew));
        
        // Edit selected
        final IconButton btnEdit = this.addRenderableWidget(new IconButton(
            ButtonType.DEFAULT,
            AreaStyle.BROWN,
            ButtonIcons.EDIT.getSprite(),
            groupDefaultModeButtons,
            guiLeft + 9,
            guiTop + 36 + 1 * ICON_BUTTON_HEIGHT,
            ICON_BUTTON_WIDTH,
            ICON_BUTTON_HEIGHT,
            null, (btn) -> {
                if (preview == null) {
                    return;
                }

                switchMode(TrafficSignWorkbenchMode.EDITOR);
                shape = preview.getShape();
                pixels = TrafficSignTextureCacheClient.textureToIntArray(preview, true);
                nameBox.setValue(preview.getName());
                selectedIndex = PatternCatalogueItem.getSelectedIndex(this.getMenu().patternSlot.getItem());
            }
        )).withAlignment(Alignment.CENTER);
        this.tooltips.get(TrafficSignWorkbenchMode.DEFAULT).add(Tooltip.of(tooltipDefaultEdit).assignedTo(btnEdit));

        // Delete selected
        final IconButton btnDelete = this.addRenderableWidget(new IconButton(
            ButtonType.DEFAULT,
            AreaStyle.BROWN,
            ButtonIcons.DELETE.getSprite(),
            groupDefaultModeButtons,
            guiLeft + 9,
            guiTop + 36 + 2 * ICON_BUTTON_HEIGHT,
            ICON_BUTTON_WIDTH,
            ICON_BUTTON_HEIGHT,
            null,
            (btn) -> {
                if (preview == null) {
                    return;
                }
                
                this.minecraft.setScreen(new ConfirmScreen((b) -> {
                    if (b) {
                        int idx = PatternCatalogueItem.getSelectedIndex(this.getMenu().patternSlot.getItem());
                        NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new PatternCatalogueDeletePacket(idx));
                    }
                    this.minecraft.setScreen(this);
                },
                Utils.translate("gui.trafficcraft.trafficsignworkbench.delete.question"),
                Utils.translate("selectWorld.deleteWarning", preview.getName()),
                Utils.translate("selectWorld.deleteButton"),
                CommonComponents.GUI_CANCEL));
            }
        )).withAlignment(Alignment.CENTER);
        this.tooltips.get(TrafficSignWorkbenchMode.DEFAULT).add(Tooltip.of(tooltipDefaultDelete).assignedTo(btnDelete));
        //#endregion

        //#region CREATE NEW PATTERN
        // Shapes
        final int x = guiLeft + (WIDTH / 2 - 18 * 2);
        final int y = guiTop + 70;
        final IconButton[] shapeButtons = Arrays.stream(TrafficSignShape.values()).map(pShape -> {
            final TrafficSignShape shape = pShape;

            final IconButton button = new IconButton(
                ButtonType.RADIO_BUTTON, 
                AreaStyle.BROWN, 
                new Sprite(shape.getIconResourceLocation(), 16, 16, 0, 0, 16, 16),
                groupShapes,
                x,
                y,
                ICON_BUTTON_WIDTH,
                ICON_BUTTON_HEIGHT,
                null,
                (btn) -> {
                    this.shape = shape;
                }
            ).withAlignment(Alignment.CENTER);
            if (shape == this.shape) {
                button.select();
            }
            this.tooltips.get(TrafficSignWorkbenchMode.CREATE_NEW).add(Tooltip.of(Utils.translate(shape.getTranslationKey())).assignedTo(button));
            return this.addRenderableWidget(button);
        }).toArray(IconButton[]::new);
        
        fillButtons(shapeButtons, 0, x, y, null);

        createNewAcceptBtn = this.addRenderableWidget(new IconButton(
            ButtonType.DEFAULT,
            AreaStyle.GRAY,
            new Sprite(OVERLAY, 256, 256, 46, 174, 16, 16),
            groupCreatePattern,
            guiLeft + WIDTH / 2 - 20,
            guiTop + 150,
            ICON_BUTTON_WIDTH,
            ICON_BUTTON_HEIGHT,
            null,
            (btn) -> {
                switchMode(TrafficSignWorkbenchMode.EDITOR);
            }).withAlignment(Alignment.CENTER)
        );

        this.addRenderableWidget(new IconButton(
            ButtonType.DEFAULT,
            AreaStyle.GRAY,
            new Sprite(OVERLAY, 256, 256, 62, 174, 16, 16),
            groupCreatePattern,
            guiLeft + WIDTH / 2 + 2,
            guiTop + 150,
            ICON_BUTTON_WIDTH,
            ICON_BUTTON_HEIGHT,
            null,
            (btn) -> {
                switchMode(TrafficSignWorkbenchMode.DEFAULT);
            }
        ).withAlignment(Alignment.CENTER));
        //#endregion

        //#region EDITOR SCREEN

        nameBox = GuiUtils.createEditBox(
            guiLeft + WIDTH / 2 - 63,
            guiTop + 164,
            120,
            10,
            font,
            name,
            false,
            (txt) -> {
                this.name = txt;
            },
            null
        );
        nameBox.setTextColor(-1);
        nameBox.setTextColorUneditable(-1);
        nameBox.setMaxLength(20);        
        addRenderableWidget(nameBox);
        groupEditor.components.add(nameBox);

        editorArea = new GuiAreaDefinition(guiLeft + WIDTH / 2 - 64 - 3, guiTop + 32, 128, 128);
        prevButton = new GuiAreaDefinition(guiLeft + 51, guiTop + 164, 23, 13);
        nextButton = new GuiAreaDefinition(guiLeft + 149, guiTop + 164, 23, 13);

        // Toolbar 1
        for (int i = 0; i < MAX_TOOLBAR1_BUTTONS; i++) {  
            final int j = i;
            Sprite sprite = null;
            switch (j) {
                case 0:
                    sprite = ButtonIcons.EDIT.getSprite();
                    break;
                case 1:
                    sprite = ButtonIcons.ERASER.getSprite();
                    break;
                case 2:
                    sprite = ButtonIcons.PICK_COLOR.getSprite();
                    break;
                case 3:
                    sprite = ButtonIcons.FILL.getSprite();
                    break;
                case 4:
                    sprite = ButtonIcons.TEXT.getSprite();
                    break;
                default:
                    break;
            }

            final IconButton btn = new IconButton(
                ButtonType.RADIO_BUTTON,
                AreaStyle.BROWN,
                sprite,
                groupEditorToolbar1,
                guiLeft + 9,
                guiTop + 36 + j * ICON_BUTTON_HEIGHT,
                ICON_BUTTON_WIDTH,
                ICON_BUTTON_HEIGHT,
                null,
                (b) -> {
                    tool = TrafficSignWorkbenchEditorTool.byIndex(j);
                }
            ).withAlignment(Alignment.CENTER);
            switch (j) {
                case 0:
                    this.tooltips.get(TrafficSignWorkbenchMode.EDITOR).add(Tooltip.of(tooltipEditorToolbarDraw).assignedTo(btn));
                    break;
                case 1:
                    this.tooltips.get(TrafficSignWorkbenchMode.EDITOR).add(Tooltip.of(tooltipEditorToolbarErase).assignedTo(btn));
                    break;
                case 2:
                    this.tooltips.get(TrafficSignWorkbenchMode.EDITOR).add(Tooltip.of(tooltipEditorToolbarPickColor).assignedTo(btn));
                    break;
                case 3:
                    this.tooltips.get(TrafficSignWorkbenchMode.EDITOR).add(Tooltip.of(tooltipEditorToolbarFill).assignedTo(btn));
                    break;
                case 4:
                    this.tooltips.get(TrafficSignWorkbenchMode.EDITOR).add(Tooltip.of(tooltipEditorToolbarText).assignedTo(btn));
                    break;
                default:
                    break;
            }
            if (this.tool == TrafficSignWorkbenchEditorTool.byIndex(j)) {
                btn.select();
            }
            this.addRenderableWidget(btn);
        } 

        // Save/Load
        for (int i = 0; i < 3; i++) {
            final int j = i;
            Sprite sprite1 = null;
            switch (j) {
                case 0:
                    sprite1 = ButtonIcons.OPEN.getSprite();
                    break;
                case 1:
                    sprite1 = ButtonIcons.SAVE.getSprite();
                    break;
                case 2:
                    sprite1 = ButtonIcons.DISCARD.getSprite();
                    break;
                default:
                    break;
            }
            final IconButton btn = new IconButton(
                ButtonType.DEFAULT,
                AreaStyle.BROWN,
                sprite1,
                groupEditorToolbar1,
                guiLeft + 9,
                guiTop + 130 + j * ICON_BUTTON_HEIGHT,
                ICON_BUTTON_WIDTH,
                ICON_BUTTON_HEIGHT,
                null,
                (button) -> {
                    switch (j) {
                        case 0:
                            this.minecraft.setScreen(new SignPickerScreen(this, shape, (image) -> {
                                if (image != null) {
                                    for (int a = 0; a < TrafficSignShape.MAX_WIDTH; a++) {
                                        for (int b = 0; b < TrafficSignShape.MAX_HEIGHT; b++) {
                                            pixels[a][b] = Utils.swapRedBlue(image.getPixelRGBA(a, b)); 
                                        }
                                    }
                                }
                            }));
                            break;
                        case 1:
                            TrafficSignData tsd = new TrafficSignData(TrafficSignShape.MAX_WIDTH, TrafficSignShape.MAX_HEIGHT, shape);
                            NativeImage img = new NativeImage(Format.RGBA, tsd.getWidth(), tsd.getHeight(), false);
                            for (int k = 0; k < tsd.getWidth(); k++) {
                                for (int l = 0; l < tsd.getHeight(); l++) {
                                    img.setPixelRGBA(k, l, 0);
                                    if (shape.isPixelValid(k, l))
                                        img.setPixelRGBA(k, l, Utils.swapRedBlue(pixels[k][l]));
                                }

                            }
                            tsd.setFromBase64(TrafficSignTextureCacheClient.textureToBase64(img));
                            tsd.setName(name);
                            img.close();
                            NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new TrafficSignPatternPacket(tsd, selectedIndex));
                            switchMode(TrafficSignWorkbenchMode.DEFAULT);
                            initPreview();
                            break;
                        case 2:
                            this.minecraft.setScreen(new ConfirmScreen((b) -> {                                
                                this.minecraft.setScreen(this);
                                if (b) {
                                    switchMode(TrafficSignWorkbenchMode.DEFAULT);
                                    initPreview();
                                }
                            },
                            Utils.translate("gui.trafficcraft.trafficsignworkbench.discard.question"),
                            Utils.emptyText(),
                            CommonComponents.GUI_YES,
                            CommonComponents.GUI_NO));
                            
                            break;
                        default:
                            break;
                    }
                }
            ).withAlignment(Alignment.CENTER);
            switch (j) {
                case 0:
                    this.tooltips.get(TrafficSignWorkbenchMode.EDITOR).add(Tooltip.of(tooltipEditorToolbarLoad).assignedTo(btn));
                    break;
                case 1:
                    this.tooltips.get(TrafficSignWorkbenchMode.EDITOR).add(Tooltip.of(tooltipEditorToolbarSave).assignedTo(btn));
                    break;
                case 2:
                    this.tooltips.get(TrafficSignWorkbenchMode.EDITOR).add(Tooltip.of(tooltipEditorToolbarDiscard).assignedTo(btn));
                    break;
                default:
                    break;
            }
            this.addRenderableWidget(btn);
        } 

        // Color picker
        this.addRenderableWidget(new IconButton(
            ButtonType.DEFAULT,
            AreaStyle.BROWN,
            ButtonIcons.ADD_SMALL.getSprite(),
            groupEditorToolbar1, 
            guiLeft + 203,
            guiTop + 36,
            ICON_BUTTON_WIDTH,
            ICON_BUTTON_HEIGHT,
            null,
            (btn) -> {
                minecraft.setScreen(new ColorPickerScreen(this, selectedColor, (c) -> {
                    this.selectedColor = c.toInt();
                }));
            }
        ) {
            @Override
            public void renderImage(PoseStack pPoseStack, int pMouseX, int pMouseY, float partialTicks) {
                super.renderImage(pPoseStack, pMouseX, pMouseY, partialTicks);
                fill(pPoseStack, getX() + 2, getY() + 2, getX() + 16, getY() + 16, selectedColor);
            }
        }.withAlignment(Alignment.CENTER));
        
        // Colors
        for (int i = 0; i < 7; i++) { 
            final int j = i;
            this.addRenderableWidget(new IconButton(
                ButtonType.DEFAULT,
                AreaStyle.BROWN,
                Sprite.empty(),
                groupColors,
                guiLeft + 203,
                guiTop + 40 + (j + 1) * ICON_BUTTON_HEIGHT,
                ICON_BUTTON_WIDTH,
                ICON_BUTTON_HEIGHT,
                null,
                (btn) -> { }
            ) {    
                @Override
                public void renderImage(PoseStack pPoseStack, int pMouseX, int pMouseY, float partialTicks) {
                    super.renderImage(pPoseStack, pMouseX, pMouseY, partialTicks);
                    ItemStack stack = menu.colorSlot.getItem();
                    if (!(stack.getItem() instanceof ColorPaletteItem))
                        return;
                    
                    fill(pPoseStack, getX() + 2, getY() + 2, getX() + 16, getY() + 16, ColorPaletteItem.getColorAt(stack, j));                    
                }

                @Override
                protected boolean isValidClickButton(int pButton) {
                    return pButton == 0 || pButton == 1 || pButton == 2;
                }

                @Override
                public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
                    if (!this.isMouseOver(pMouseX, pMouseY)) {
                        return super.mouseClicked(pMouseX, pMouseY, pButton);
                    }

                    ItemStack stack = menu.colorSlot.getItem();
                    if (!(stack.getItem() instanceof ColorPaletteItem))
                        return super.mouseClicked(pMouseX, pMouseY, pButton);

                    switch (pButton) {
                        case 0:
                            int color = ColorPaletteItem.getColorAt(stack, j);
                            selectedColor = color == 0 ? selectedColor : color;
                            break;
                        case 1:
                            NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new ColorPaletteItemPacket(selectedColor, j));
                            break;
                        case 2:
                            NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new ColorPaletteItemPacket(0, j));
                            break;
                        default:
                            break;
                    }
                    return super.mouseClicked(pMouseX, pMouseY, pButton);
                }
            }.withAlignment(Alignment.CENTER));
        }
        //#endregion

        switchMode(mode);
    }

    private void fillButtons(IconButton[] buttons, int scrollRow, int defX, int defY, VerticalScrollBar scrollbar) {
        int currentRow = -1;
        for (int i = 0; i < buttons.length; i++) {
            if (i % MAX_ENTRIES_IN_ROW == 0)
                currentRow++;

            buttons[i].setX(defX + (i % MAX_ENTRIES_IN_ROW) * ICON_BUTTON_WIDTH);
            buttons[i].setY(defY + (currentRow) * ICON_BUTTON_HEIGHT - (scrollRow * ICON_BUTTON_HEIGHT));
            buttons[i].visible = currentRow >= scrollRow && currentRow < scrollRow + MAX_ROWS;
        }

        if (scrollbar != null) {
            scrollbar.setMaxRowsOnPage(MAX_ROWS).updateMaxScroll(currentRow + 1);
        }
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return (getFocused() instanceof EditBox && this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    protected void containerTick() {        
        super.containerTick();

        // On Item take/set
        if (this.getMenu().patternSlot.hasItem() && this.mode == TrafficSignWorkbenchMode.EMPTY) {
            switchMode(TrafficSignWorkbenchMode.DEFAULT);
        } else if (!this.getMenu().patternSlot.hasItem()) {
            switchMode(TrafficSignWorkbenchMode.EMPTY);
        }

        switch (this.mode) {
            case EDITOR:
                groupColors.setVisible(this.getMenu().colorSlot.hasItem());
                break;
            case DEFAULT:
                groupDefaultModeButtons.components.get(0).active = !isFull();
                groupDefaultModeButtons.components.get(1).active = preview != null;
                groupDefaultModeButtons.components.get(2).active = preview != null;
                break;
            default:
                break;
        }

        if (createNewAcceptBtn.visible) {
            createNewAcceptBtn.active = shape != null;
        }
    }

    private boolean isFull() {
        return this.getMenu().patternSlot.getItem().getItem() instanceof PatternCatalogueItem && PatternCatalogueItem.getStoredPatternCount(this.getMenu().patternSlot.getItem()) >= ((PatternCatalogueItem)this.getMenu().patternSlot.getItem().getItem()).getMaxPatterns();
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        blit(pPoseStack, guiLeft, guiTop, 0, 0, WIDTH, HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // Render mode
        switch (this.mode) {
            case DEFAULT:
                renderPatternBackground(pPoseStack);    

                // render arrow
                blit(pPoseStack, prevButton.getX(), prevButton.getY(), prevButton.isInBounds(pMouseX, pMouseY) ? 23 : 0, 174 + 13, prevButton.getWidth(), prevButton.getHeight(), 256, 256); //right
                blit(pPoseStack, nextButton.getX(), nextButton.getY(), nextButton.isInBounds(pMouseX, pMouseY) ? 23 : 0, 174, nextButton.getWidth(), nextButton.getHeight(), 256, 256); //left
                
                // render pattern count            
                String label = String.format("%s / %s", PatternCatalogueItem.getSelectedIndex(this.getMenu().patternSlot.getItem()) + 1, PatternCatalogueItem.getStoredPatternCount(this.getMenu().patternSlot.getItem()));
                this.font.draw(pPoseStack, label, guiLeft + WIDTH / 2 - font.width(label) / 2, guiTop + 170 - font.lineHeight / 2, 4210752);
                label = preview == null ? "" : preview.getName();
                this.font.draw(pPoseStack, label, guiLeft + WIDTH / 2 - font.width(label) / 2, guiTop + 155 - font.lineHeight / 2, 4210752);

                // render preview image
                if (preview != null) {  
                    this.preview.render(pPoseStack, guiLeft + WIDTH / 2 - 50, guiTop + 40, 100, 100);
                } else {
                    label = emptyPattern.getString();
                    this.font.draw(pPoseStack, label, guiLeft + WIDTH / 2 - font.width(label) / 2, guiTop + 100 - font.lineHeight / 2, 4210752);
                }

                // render buttons bg
                DynamicGuiRenderer.renderArea(pPoseStack, guiLeft + 8, guiTop + 35, 20, 56, AreaStyle.BROWN, ButtonState.SUNKEN);
                break;
            case CREATE_NEW:
                renderPatternBackground(pPoseStack);
                DynamicGuiRenderer.renderArea(pPoseStack, guiLeft + (WIDTH / 2 - 18 * 2) - 1, guiTop + 69, 18 * 4 + 2, 2 + (TrafficSignShape.values().length / 4 + (TrafficSignShape.values().length % 4 == 0 ? 0 : 1)) * 18, AreaStyle.BROWN, ButtonState.SUNKEN);
                this.font.draw(pPoseStack, createPattern, guiLeft + WIDTH / 2 - font.width(createPattern) / 2, guiTop + 40 - font.lineHeight / 2, 4210752);
                this.font.draw(pPoseStack, createPatternInstruction, guiLeft + WIDTH / 2 - font.width(createPatternInstruction) / 2, guiTop + 55 - font.lineHeight / 2, 4210752);
                break;
            case EDITOR:
                renderPatternBackground(pPoseStack);  
                DynamicGuiRenderer.renderArea(pPoseStack, guiLeft + 8, guiTop + 35, 20, 18 * 4 + 2, AreaStyle.BROWN, ButtonState.SUNKEN);
                DynamicGuiRenderer.renderArea(pPoseStack, guiLeft + 8, guiTop + 129, 20, 18 * 3 + 2, AreaStyle.BROWN, ButtonState.SUNKEN);
                DynamicGuiRenderer.renderArea(pPoseStack, guiLeft + 202, guiTop + 35, 20, 20, AreaStyle.BROWN, ButtonState.SUNKEN);   
                DynamicGuiRenderer.renderArea(pPoseStack, guiLeft + 202, guiTop + 57, 20, 18 * 7 + 2, AreaStyle.BROWN, ButtonState.SUNKEN);           
                DynamicGuiRenderer.renderArea(pPoseStack, guiLeft + WIDTH / 2 - 65, guiTop + 162, 120, 12, AreaStyle.GRAY, ButtonState.SUNKEN); // textbox
                GuiUtils.setShaderColor(0, 0, 0, 1);

                // render shape
                GuiUtils.blit(shape.getShapeTextureId(), pPoseStack, editorArea.getX() - 1, editorArea.getY() - 1, editorArea.getWidth(), editorArea.getHeight(), 0, 0, 32, 32, 32, 32);
                GuiUtils.blit(shape.getShapeTextureId(), pPoseStack, editorArea.getX() + 1, editorArea.getY() - 1, editorArea.getWidth(), editorArea.getHeight(), 0, 0, 32, 32, 32, 32);
                GuiUtils.blit(shape.getShapeTextureId(), pPoseStack, editorArea.getX() - 1, editorArea.getY() + 1, editorArea.getWidth(), editorArea.getHeight(), 0, 0, 32, 32, 32, 32);
                GuiUtils.blit(shape.getShapeTextureId(), pPoseStack, editorArea.getX() + 1, editorArea.getY() + 1, editorArea.getWidth(), editorArea.getHeight(), 0, 0, 32, 32, 32, 32);
                GuiUtils.setShaderColor(1, 1, 1, 1);
                GuiUtils.blit(shape.getShapeTextureId(), pPoseStack, editorArea.getX(), editorArea.getY(), editorArea.getWidth(), editorArea.getHeight(), 0, 0, 32, 32, 32, 32);

                // render pixels
                for (int a = 0; a < pixels.length; a++) {
                    for (int b = 0; b < pixels[a].length; b++) {
                        fill(pPoseStack,
                            editorArea.getX() + a * 4,
                            editorArea.getY() + b * 4,
                            editorArea.getX() + a * 4 + 4,
                            editorArea.getY() + b * 4 + 4,
                            pixels[a][b]);
                    }
                }

                // render cursor
                if (editorArea.isInBounds(pMouseX, pMouseY)) {
                    int mX = getMouseXEditorCoord(pMouseX);
                    int mY = getMouseYEditorCoord(pMouseY);

                    if (shape.isPixelValid(mX, mY)) {
                        int x = editorArea.getX() + mX * 4;
                        int y = editorArea.getY() + mY * 4;
                        fill(pPoseStack, x, y, x + 4, y + 4, 0x7F000000);
                    }
                }
                break;
            default:
                break;
        }
                
        this.font.draw(pPoseStack, title.getString(), guiLeft + WIDTH / 2 - font.width(title) / 2, guiTop + 5, 4210752);
        
    }

    private void renderPatternBackground(PoseStack pPoseStack) {
        GuiUtils.blit(OVERLAY, pPoseStack, guiLeft + 36, guiTop + 14, 0, 0, 158, 174, 256, 256);
    }

    private int getMouseXEditorCoord(double mouseX) {
        return ((int)mouseX - editorArea.getX()) / 4;
    }

    private int getMouseYEditorCoord(double mouseY) {
        return ((int)mouseY - editorArea.getY()) / 4;
    }

    private void draw(double pMouseX, double pMouseY, int button) {
        if (mode != TrafficSignWorkbenchMode.EDITOR || !editorArea.isInBounds(pMouseX, pMouseY))
            return;

        int x = getMouseXEditorCoord(pMouseX);
        int y = getMouseYEditorCoord(pMouseY);
        
        switch (tool) {
            case DRAW:
            case ERASER:
            default:
                int color = this.selectedColor;
                if (button == 1 || tool == TrafficSignWorkbenchEditorTool.ERASER) {
                    color = 0;
                }

                if (shape.isPixelValid(x, y)) {
                    pixels[x][y] = color;
                }
                break;
            case PICK_COLOR:
                if (shape.isPixelValid(x, y) && pixels[x][y] != 0) {
                    selectedColor = pixels[x][y];
                }
                break;
            case FILL:
                if (shape.isPixelValid(x, y)) {
                    fillArea(x, y, pixels[x][y]);
                    tool = TrafficSignWorkbenchEditorTool.DRAW;
                    for (int w = 0; w < groupEditorToolbar1.components.size(); w++) {
                        if (groupEditorToolbar1.components.get(w) instanceof IconButton btn) {
                            if (w == 0) {
                                btn.select();
                            } else {
                                btn.deselect();
                            }
                        }
                    }
                }
                break;
        }
    }

    private void fillArea(int x, int y, final int replaceColor) {
        if (shape.isPixelValid(x, y) && pixels[x][y] != selectedColor) {
            pixels[x][y] = selectedColor;
        } else {
            return;
        }

        int x1 = x - 1;
        if (x1 >= 0 && shape.isPixelValid(x1, y) && pixels[x1][y] == replaceColor)
            fillArea(x - 1, y, replaceColor);
        x1 = x + 1;
        if (x1 < TrafficSignShape.MAX_WIDTH && shape.isPixelValid(x1, y) && pixels[x1][y] == replaceColor)
            fillArea(x + 1, y, replaceColor);
        int y1 = y - 1;
        if (y1 >= 0 && shape.isPixelValid(x, y1) && pixels[x][y1] == replaceColor)
            fillArea(x, y - 1, replaceColor);
        y1 = y + 1;
        if (y1 < TrafficSignShape.MAX_HEIGHT && shape.isPixelValid(x, y1) && pixels[x][y1] == replaceColor)
            fillArea(x, y + 1, replaceColor);
    }

    private void switchPreview(int index) {
        PatternCatalogueItem.setSelectedIndex(this.getMenu().patternSlot.getItem(), index);
        NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new PatternCatalogueIndexPacketGui(index));
        initPreview();
    }

    private void initPreview() {
        if (preview != null) {
            preview.close();
            preview = null;
        }

        this.preview = PatternCatalogueItem.getSelectedPattern(this.getMenu().patternSlot.getItem());
    }

    public void updatePreview() {
        this.initPreview();
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pPoseStack, pMouseX, pMouseY);
        
        switch (this.mode) {
            case DEFAULT:
                //Utils.renderTooltip(this, groupDefaultModeButtons.components.get(0), () -> isFull() ? List.of(tooltipDefaultNewFull1.withStyle(ChatFormatting.RED).getVisualOrderText(), tooltipDefaultNewFull2.withStyle(ChatFormatting.GRAY).getVisualOrderText()) : List.of(tooltipDefaultNew.getVisualOrderText()), pPoseStack, pMouseX, pMouseY);
                //Utils.renderTooltip(this, groupDefaultModeButtons.components.get(1), () -> List.of(tooltipDefaultEdit.getVisualOrderText()), pPoseStack, pMouseX, pMouseY);
                //Utils.renderTooltip(this, groupDefaultModeButtons.components.get(2), () -> List.of(tooltipDefaultDelete.getVisualOrderText()), pPoseStack, pMouseX, pMouseY);
                break;
            case CREATE_NEW:

                break;
            case EDITOR:
                for (int i = 0; i < groupEditorToolbar1.components.size(); i++) {
                    //final int j = i;
                    //Utils.renderTooltip(this, groupEditorToolbar1.components.get(j), () -> List.of(editorToolbar1Tooltips[j]), pPoseStack, pMouseX, pMouseY);
                }

                //Color tooltips
                for (int i = 0; i < groupColors.components.size(); i++) {
                    final int j = i;
                    ItemStack stack = menu.colorSlot.getItem();
                    if (!(stack.getItem() instanceof ColorPaletteItem))
                        break;
                    int color = ColorPaletteItem.getColorAt(stack, j);
                    if (color == 0) {
                        color = 0xFFFFFFFF;
                    }
                    final int c = color;

                    GuiUtils.renderTooltip(this, groupColors.components.get(j), List.of(
                        Utils.translate("gui.trafficcraft.trafficsignworkbench.editor.color.slot", j + 1).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(c))),
                        Utils.translate("gui.trafficcraft.trafficsignworkbench.editor.color.get").withStyle(ChatFormatting.GRAY),
                        Utils.translate("gui.trafficcraft.trafficsignworkbench.editor.color.set").withStyle(ChatFormatting.GRAY),
                        Utils.translate("gui.trafficcraft.trafficsignworkbench.editor.color.remove").withStyle(ChatFormatting.GRAY)
                    ), width / 4, pPoseStack, pMouseX, pMouseY);
                }
                break;
            default:
                break;
        }

        this.tooltips.get(this.mode).forEach((x) -> {
            x.render(this, pPoseStack, pMouseX, pMouseY);
        });
    }


    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        switch (mode) {
            case DEFAULT:
                if (nextButton.isInBounds(pMouseX, pMouseY)) {
                    switchPreview(PatternCatalogueItem.getSelectedIndex(this.getMenu().patternSlot.getItem()) + 1);
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.2F));
                } else if (prevButton.isInBounds(pMouseX, pMouseY)) {
                    switchPreview(Math.max(PatternCatalogueItem.getSelectedIndex(this.getMenu().patternSlot.getItem()) - 1, 0));
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.2F));
                }
                break;
            case EDITOR:
                draw(pMouseX, pMouseY, pButton);
                break;
            default:
                break;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    
    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        draw(pMouseX, pMouseY, pButton);
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @SuppressWarnings("unused")
    private boolean isMouseInBounds(int x, int y, int w, int h, int mX, int mY) {
        return mX >= x && mX <= x + w && mY >= y && mY <= y + h;
    }

    private void switchMode(TrafficSignWorkbenchMode mode) {
        boolean differentMode = this.mode != mode;
        this.mode = mode;
        groupDefaultModeButtons.setVisible(false);
        groupShapes.setVisible(false);
        groupCreatePattern.setVisible(false);
        groupEditor.setVisible(false);
        groupEditorToolbar1.setVisible(false);
        groupColors.setVisible(false);

        switch (mode) {
            default:
            case EMPTY:
                break;
            case DEFAULT:
                groupDefaultModeButtons.setVisible(true);
                break;
            case CREATE_NEW:
                groupShapes.setVisible(true);
                groupCreatePattern.setVisible(true);
                break;
            case EDITOR:
                groupEditor.setVisible(true);
                groupEditorToolbar1.setVisible(true);
                groupColors.setVisible(true);
                break;
        }

        if (differentMode) {
            this.initMode();
        }
    }

    private void initMode() {
        switch (mode) {
            default:
            case EMPTY:
                break;
            case DEFAULT:
                initPreview();
                break;
            case CREATE_NEW:
                this.shape = null;
                groupShapes.performForEachOfType(IconButton.class, w -> w.deselect());
                break;
            case EDITOR:
                pixels = new int[TrafficSignShape.MAX_WIDTH][];
                for (int a = 0; a < pixels.length; a++) {
                    pixels[a] = new int[TrafficSignShape.MAX_HEIGHT];
                }
                name = Utils.translate("gui.trafficcraft.trafficsignworkbench.pattern.name_unknown").getString();
                nameBox.setValue(name);
                selectedIndex = -1;
                break;
        }
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

    protected enum ButtonIcons {
        EDIT(0),
        ERASER(1),
        PICK_COLOR(2),
        TEXT(3),
        FILL(4),
        DELETE(5),
        PATTERN(6),
        ADD(7),
        ADD_SMALL(8),
        SAVE(9),
        OPEN(10),
        IMPORT(11),
        DISCARD(12);

        private int index;

        private static final int U = 238;
        private static final int ICON_SIZE = 18;

        private ButtonIcons(int index) {
            this.index = index;
        }

        public int getV() {
            return this.getIndex() * ICON_SIZE;
        }

        public int getU() {
            return U;
        }

        public int getIndex() {
            return index;
        }

        public static ButtonIcons byIndex(int index) {
            if (index < 0 || index >= values().length) {
                return ButtonIcons.EDIT;
            }
            return ButtonIcons.values()[index];
        }

        public void render(PoseStack pPoseStack, int x, int y) {   
            RenderSystem.setShaderTexture(0, OVERLAY);         
            blit(pPoseStack, x, y, this.getU(), this.getV(), ICON_SIZE, ICON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        public Sprite getSprite() {
            return new Sprite(OVERLAY, 256, 256, this.getU(), this.getV(), ICON_SIZE, ICON_SIZE);
        }
    }
}

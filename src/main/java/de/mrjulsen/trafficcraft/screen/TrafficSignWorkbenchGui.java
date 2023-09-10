package de.mrjulsen.trafficcraft.screen;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.properties.TrafficSignShape;
import de.mrjulsen.trafficcraft.data.TrafficSignData;
import de.mrjulsen.trafficcraft.item.ColorPaletteItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.ColorPaletteItemPacket;
import de.mrjulsen.trafficcraft.network.packets.PatternCatalogueDeletePacket;
import de.mrjulsen.trafficcraft.network.packets.PatternCatalogueIndexPacketGui;
import de.mrjulsen.trafficcraft.network.packets.TrafficSignPatternPacket;
import de.mrjulsen.trafficcraft.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.screen.widgets.IconButton.ButtonType;
import de.mrjulsen.trafficcraft.util.Utils;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer;
import de.mrjulsen.trafficcraft.screen.widgets.ControlCollection;
import de.mrjulsen.trafficcraft.screen.widgets.GuiAreaDefinition;
import de.mrjulsen.trafficcraft.screen.widgets.HScrollBar;
import de.mrjulsen.trafficcraft.screen.widgets.ICustomAreaControl;
import de.mrjulsen.trafficcraft.screen.widgets.IconButton;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer.AreaStyle;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer.ColorStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrafficSignWorkbenchGui extends AbstractContainerScreen<TrafficSignWorkbenchMenu> {

    public static final Component title = new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.title");

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int WIDTH = 230;
    private static final int HEIGHT = 256;

    private static final int MAX_ENTRIES_IN_ROW = 4;
    private static final int MAX_ROWS = 3;
    
    private static final int ICON_BUTTON_WIDTH = 18;
    private static final int ICON_BUTTON_HEIGHT = 18;


    // Groups
    private final ControlCollection groupDefaultModeButtons = new ControlCollection();
    private final ControlCollection groupEditor = new ControlCollection();
    private final ControlCollection groupEditorToolbar1 = new ControlCollection();
    private final ControlCollection groupShapes = new ControlCollection();
    private final ControlCollection groupCreatePattern = new ControlCollection();
    private final ControlCollection groupColors = new ControlCollection();
    
    // gui
    private int guiLeft;
    private int guiTop;
    private TrafficSignWorkbenchMode mode = TrafficSignWorkbenchMode.EMPTY; 
    private GuiAreaDefinition editorArea, nextButton, prevButton;
    private TrafficSignData preview;
    private EditBox nameBox;

    // data
    private TrafficSignShape shape;
    private int[][] pixels; // image
    private String name;
    private TrafficSignWorkbenchEditorTool tool = TrafficSignWorkbenchEditorTool.DRAW;
    private int selectedColor = 0xFF000000;
    private int selectedIndex = -1;

    // texts
    private final TranslatableComponent createPattern = new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.createpattern.title");
    private final TranslatableComponent createPatternInstruction = new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.createpattern.instruction");
    private final TranslatableComponent emptyPattern = new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.menu.no_pattern");

    // tooltips
    private final FormattedCharSequence[] defaultModeButtonsTooltips = new FormattedCharSequence[] {
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.menu.add").getVisualOrderText(),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.menu.edit").getVisualOrderText(),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.menu.delete").getVisualOrderText()
    };

    private final FormattedCharSequence[] editorToolbar1Tooltips = new FormattedCharSequence[] {
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.editor.draw").getVisualOrderText(),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.editor.erase").getVisualOrderText(),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.editor.pick_color").getVisualOrderText(),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.editor.fill").getVisualOrderText(),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.editor.load").getVisualOrderText(),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.editor.save").getVisualOrderText(),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.editor.pick_color").getVisualOrderText()
    };

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

        //#region DEFAULT MODE CONTROLS
        // Add new
        this.addRenderableWidget(new IconButton(ButtonType.DEFAULT, ColorStyle.BROWN, groupDefaultModeButtons, guiLeft + 9, guiTop + 36 + 0 * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, playerInventoryTitle, (btn) -> {
            switchMode(TrafficSignWorkbenchMode.CREATE_NEW);
        }) {
            @Override
            protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                ButtonIcons.ADD.render(pPoseStack, x, y);
            }
        });
        
        // Edit selected
        this.addRenderableWidget(new IconButton(ButtonType.DEFAULT, ColorStyle.BROWN, groupDefaultModeButtons, guiLeft + 9, guiTop + 36 + 1 * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, playerInventoryTitle, (btn) -> {
            if (preview == null) {
                return;
            }

            switchMode(TrafficSignWorkbenchMode.EDITOR);
            shape = preview.getShape();
            pixels = preview.textureToIntArray(true);
            nameBox.setValue(preview.getName());
            selectedIndex = PatternCatalogueItem.getSelectedIndex(this.getMenu().patternSlot.getItem());
        }) {
            @Override
            protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                ButtonIcons.EDIT.render(pPoseStack, x, y);
            }
        });

        // Delete selected
        this.addRenderableWidget(new IconButton(ButtonType.DEFAULT, ColorStyle.BROWN, groupDefaultModeButtons, guiLeft + 9, guiTop + 36 + 2 * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, playerInventoryTitle, (btn) -> {
            if (preview == null) {
                return;
            }
            
            this.minecraft.setScreen(new ConfirmScreen((b) -> {
            if (b) {
                int idx = PatternCatalogueItem.getSelectedIndex(this.getMenu().patternSlot.getItem());
                NetworkManager.MOD_CHANNEL.sendToServer(new PatternCatalogueDeletePacket(idx));
            }

            this.minecraft.setScreen(this);
         }, new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.delete.question"), new TranslatableComponent("selectWorld.deleteWarning", preview.getName()), new TranslatableComponent("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));

            
        }) {
            @Override
            protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                ButtonIcons.DELETE.render(pPoseStack, x, y);
            }
        });
        //#endregion

        //#region CREATE NEW PATTERN
        // Shapes
        final int x = guiLeft + (WIDTH / 2 - 18 * 2);
        final int y = guiTop + 70;
        final IconButton[] shapeButtons = Arrays.stream(TrafficSignShape.values()).map(pShape -> {
            final TrafficSignShape shape = pShape;
            IconButton button = new IconButton(ButtonType.RADIO_BUTTON, ColorStyle.BROWN, groupShapes, x, y, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, playerInventoryTitle, (btn) -> {
                this.shape = shape;
            }) {
                @Override
                protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                    super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                    RenderSystem.setShaderTexture(0, shape.getIconResourceLocation());
                    blit(pPoseStack, x + 1, y + 1, 0, 0, 16, 16, 16, 16);
                }
            };
            if (shape == this.shape) {
                button.select();
            }
            return this.addRenderableWidget(button);
        }).toArray(IconButton[]::new);
        
        fillButtons(shapeButtons, 0, x, y, null);

        this.addRenderableWidget(new IconButton(ButtonType.DEFAULT, ColorStyle.GRAY, groupCreatePattern, guiLeft + WIDTH / 2 - 20, guiTop + 150, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, playerInventoryTitle, (btn) -> {
            switchMode(TrafficSignWorkbenchMode.EDITOR);
        }) {
            @Override
            protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                RenderSystem.setShaderTexture(0, OVERLAY);
                blit(pPoseStack, x + 1, y + 1, 46, 174, 16, 16, 256, 256);

                this.active = shape != null;
            }
        });

        this.addRenderableWidget(new IconButton(ButtonType.DEFAULT, ColorStyle.GRAY, groupCreatePattern, guiLeft + WIDTH / 2 + 2, guiTop + 150, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, playerInventoryTitle, (btn) -> {
            switchMode(TrafficSignWorkbenchMode.DEFAULT);
        }) {
            @Override
            protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                RenderSystem.setShaderTexture(0, OVERLAY);
                blit(pPoseStack, x + 1, y + 1, 62, 174, 16, 16, 256, 256);
            }
        });
        //#endregion

        //#region EDITOR SCREEN

        nameBox = new EditBox(this.font, guiLeft + WIDTH / 2 - 63, guiTop + 164, 120, 10, new TranslatableComponent("container.repair"));
        nameBox.setTextColor(-1);
        nameBox.setTextColorUneditable(-1);
        nameBox.setBordered(false);
        nameBox.setMaxLength(50);
        nameBox.setValue(name);
        nameBox.setResponder(n -> {
            this.name = n;
        });
        nameBox.setMaxLength(20);
        
        addRenderableWidget(nameBox);
        groupEditor.components.add(nameBox);

        editorArea = new GuiAreaDefinition(guiLeft + WIDTH / 2 - 64 - 3, guiTop + 32, 128, 128);
        prevButton = new GuiAreaDefinition(guiLeft + 51, guiTop + 164, 23, 13);
        nextButton = new GuiAreaDefinition(guiLeft + 149, guiTop + 164, 23, 13);

        // Toolbar 1
        for (int i = 0; i < 4; i++) {  
            final int j = i;
            IconButton btn = new IconButton(ButtonType.RADIO_BUTTON, ColorStyle.BROWN, groupEditorToolbar1, guiLeft + 9, guiTop + 36 + j * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, playerInventoryTitle, (b) -> {
                tool = TrafficSignWorkbenchEditorTool.byIndex(j);
            }) {
                @Override
                protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                    super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                    switch (j) {
                        case 0:
                            ButtonIcons.EDIT.render(pPoseStack, x, y);
                            break;
                        case 1:
                            ButtonIcons.ERASER.render(pPoseStack, x, y);
                            break;
                        case 2:
                            ButtonIcons.PICK_COLOR.render(pPoseStack, x, y);
                            break;
                        case 3:
                            ButtonIcons.FILL.render(pPoseStack, x, y);
                            break;
                        case 4:
                            ButtonIcons.TEXT.render(pPoseStack, x, y);
                            break;
                        default:
                            break;
                    }
                }
            };

            if (this.tool == TrafficSignWorkbenchEditorTool.byIndex(j)) {
                btn.select();
            }

            this.addRenderableWidget(btn);
        } 

        // Save/Load
        for (int i = 0; i < 2; i++) {  
            final int j = i;
            IconButton btn = new IconButton(ButtonType.DEFAULT, ColorStyle.BROWN, groupEditorToolbar1, guiLeft + 9, guiTop + 148 + j * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, playerInventoryTitle, (b) -> {
                switch (j) {
                    case 0:
                        this.minecraft.setScreen(new SignPickerScreen(this, shape));
                        break;
                    case 1:
                        TrafficSignData data = new TrafficSignData(TrafficSignShape.MAX_WIDTH, TrafficSignShape.MAX_HEIGHT, shape);
                        for (int k = 0; k < TrafficSignShape.MAX_WIDTH; k++) {
                            for (int l = 0; l < TrafficSignShape.MAX_HEIGHT; l++) {
                                data.setPixelRGBA(k, l, 0);
                                if (shape.isPixelValid(k, l))
                                    data.setPixelRGBA(k, l, Utils.swapRedBlue(pixels[k][l]));
                            }
                        }
                        data.setName(name);
                        NetworkManager.MOD_CHANNEL.sendToServer(new TrafficSignPatternPacket(data, selectedIndex));
                        switchMode(TrafficSignWorkbenchMode.DEFAULT);
                        break;
                    default:
                        break;
                }
            }) {
                @Override
                protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                    super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                    switch (j) {
                        case 0:
                            ButtonIcons.OPEN.render(pPoseStack, x, y);
                            break;
                        case 1:
                            ButtonIcons.SAVE.render(pPoseStack, x, y);
                            break;
                        default:
                            break;
                    }
                }
            };

            this.addRenderableWidget(btn);
        } 

        // Color picker
        this.addRenderableWidget(new IconButton(ButtonType.DEFAULT, ColorStyle.BROWN, groupEditorToolbar1, guiLeft + 203, guiTop + 36, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, playerInventoryTitle, (btn) -> {
            minecraft.setScreen(new ColorPickerGui(this, selectedColor, (c) -> {
                this.selectedColor = c.toInt();
            }));
        }) {
            @Override
            protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                fill(pPoseStack, x + 2, y + 2, x + 16, y + 16, selectedColor);
                ButtonIcons.ADD_SMALL.render(pPoseStack, x, y);
            }
        });

        
        // Colors
        for (int i = 0; i < 7; i++) { 
            final int j = i;
            this.addRenderableWidget(new IconButton(ButtonType.DEFAULT, ColorStyle.BROWN, groupColors, guiLeft + 203, guiTop + 40 + (j + 1) * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, playerInventoryTitle, (btn) -> { }) {    
                @Override
                protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                    super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                    ItemStack stack = menu.colorSlot.getItem();
                    if (!(stack.getItem() instanceof ColorPaletteItem))
                        return;
                    
                    fill(pPoseStack, x + 2, y + 2, x + 16, y + 16, ColorPaletteItem.getColorAt(stack, j));                    
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
                            NetworkManager.MOD_CHANNEL.sendToServer(new ColorPaletteItemPacket(selectedColor, j));
                            break;
                        case 2:
                            NetworkManager.MOD_CHANNEL.sendToServer(new ColorPaletteItemPacket(0, j));
                            break;
                        default:
                            break;
                    }
                    return super.mouseClicked(pMouseX, pMouseY, pButton);
                }
            });
        }
        //#endregion

        switchMode(mode);
    }

    private void fillButtons(IconButton[] buttons, int scrollRow, int defX, int defY, HScrollBar scrollbar) {
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

    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (this.shouldCloseOnEsc() && p_keyPressed_1_ == 256 || this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_))) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
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
                groupDefaultModeButtons.components.get(1).active = preview != null;
                groupDefaultModeButtons.components.get(2).active = preview != null;
                break;
            default:
                break;
        }
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
                AreaRenderer.renderArea(pPoseStack, guiLeft + 8, guiTop + 35, 20, 56, ColorStyle.BROWN, AreaStyle.SUNKEN);
                break;
            case CREATE_NEW:
                renderPatternBackground(pPoseStack);
                AreaRenderer.renderArea(pPoseStack, guiLeft + (WIDTH / 2 - 18 * 2) - 1, guiTop + 69, 18 * 4 + 2, 2 + (TrafficSignShape.values().length / 4 + (TrafficSignShape.values().length % 4 == 0 ? 0 : 1)) * 18, ColorStyle.BROWN, AreaStyle.SUNKEN);
                this.font.draw(pPoseStack, createPattern, guiLeft + WIDTH / 2 - font.width(createPattern) / 2, guiTop + 40 - font.lineHeight / 2, 4210752);
                this.font.draw(pPoseStack, createPatternInstruction, guiLeft + WIDTH / 2 - font.width(createPatternInstruction) / 2, guiTop + 55 - font.lineHeight / 2, 4210752);
                break;
            case EDITOR:
                renderPatternBackground(pPoseStack);  
                AreaRenderer.renderArea(pPoseStack, guiLeft + 8, guiTop + 35, 20, 18 * 4 + 2, ColorStyle.BROWN, AreaStyle.SUNKEN);
                AreaRenderer.renderArea(pPoseStack, guiLeft + 8, guiTop + 147, 20, 18 * 2 + 2, ColorStyle.BROWN, AreaStyle.SUNKEN);
                AreaRenderer.renderArea(pPoseStack, guiLeft + 202, guiTop + 35, 20, 20, ColorStyle.BROWN, AreaStyle.SUNKEN);   
                AreaRenderer.renderArea(pPoseStack, guiLeft + 202, guiTop + 57, 20, 18 * 7 + 2, ColorStyle.BROWN, AreaStyle.SUNKEN);           
                AreaRenderer.renderArea(pPoseStack, guiLeft + WIDTH / 2 - 65, guiTop + 162, 120, 12, ColorStyle.GRAY, AreaStyle.SUNKEN); // textbox
                RenderSystem.setShaderTexture(0, shape.getShapeTextureId());
                RenderSystem.setShaderColor(0, 0, 0, 1);

                // render shape
                blit(pPoseStack, editorArea.getX() - 1, editorArea.getY() - 1, editorArea.getWidth(), editorArea.getHeight(), 0, 0, 32, 32, 32, 32);
                blit(pPoseStack, editorArea.getX() + 1, editorArea.getY() - 1, editorArea.getWidth(), editorArea.getHeight(), 0, 0, 32, 32, 32, 32);
                blit(pPoseStack, editorArea.getX() - 1, editorArea.getY() + 1, editorArea.getWidth(), editorArea.getHeight(), 0, 0, 32, 32, 32, 32);
                blit(pPoseStack, editorArea.getX() + 1, editorArea.getY() + 1, editorArea.getWidth(), editorArea.getHeight(), 0, 0, 32, 32, 32, 32);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                blit(pPoseStack, editorArea.getX(), editorArea.getY(), editorArea.getWidth(), editorArea.getHeight(), 0, 0, 32, 32, 32, 32);

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
        RenderSystem.setShaderTexture(0, OVERLAY);
        blit(pPoseStack, guiLeft + 36, guiTop + 14, 0, 0, 158, 174, 256, 256);
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
        NetworkManager.MOD_CHANNEL.sendToServer(new PatternCatalogueIndexPacketGui(index));
        initPreview();
    }

    private void initPreview() {
        if (preview != null) {
            preview.close();
            preview = null;
        }

        this.preview = PatternCatalogueItem.getSelectedPattern(this.getMenu().patternSlot.getItem());
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack, pMouseY);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pPoseStack, pMouseX, pMouseY);
        
        switch (this.mode) {
            case DEFAULT:
                for (int i = 0; i < groupDefaultModeButtons.components.size(); i++) {
                    final int j = i;
                    Utils.renderTooltip(this, groupDefaultModeButtons.components.get(j), () -> List.of(defaultModeButtonsTooltips[j]), pPoseStack, pMouseX, pMouseY);
                }
                break;
            case CREATE_NEW:

                break;
            case EDITOR:
                for (int i = 0; i < groupEditorToolbar1.components.size(); i++) {
                    final int j = i;
                    Utils.renderTooltip(this, groupEditorToolbar1.components.get(j), () -> List.of(editorToolbar1Tooltips[j]), pPoseStack, pMouseX, pMouseY);
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

                    Utils.renderTooltip(this, groupColors.components.get(j), () -> List.of(
                        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.editor.color.slot", j + 1).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(c))).getVisualOrderText(),
                        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.editor.color.get").withStyle(ChatFormatting.GRAY).getVisualOrderText(),
                        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.editor.color.set").withStyle(ChatFormatting.GRAY).getVisualOrderText(),
                        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.editor.color.remove").withStyle(ChatFormatting.GRAY).getVisualOrderText()
                    ), pPoseStack, pMouseX, pMouseY);
                }
                break;
            default:
                break;
        }
    }


    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        switch (mode) {
            case DEFAULT:
                if (nextButton.isInBounds(pMouseX, pMouseY)) {
                    switchPreview(PatternCatalogueItem.getSelectedIndex(this.getMenu().patternSlot.getItem()) + 1);
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.2F));
                } else if (prevButton.isInBounds(pMouseX, pMouseY)) {
                    switchPreview(PatternCatalogueItem.getSelectedIndex(this.getMenu().patternSlot.getItem()) - 1);
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

        boolean[] b = new boolean[] { false };
        this.renderables.stream().filter(x -> x instanceof GuiEventListener).forEach(x -> {
            if (((GuiEventListener)x).mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY) && !b[0])
                b[0] = true;
        });
        super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        return b[0];
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        boolean[] b = new boolean[] { false };
        this.renderables.stream().filter(x -> x instanceof ICustomAreaControl && x instanceof GuiEventListener).forEach(x -> {
            if (((ICustomAreaControl)x).isInArea(pMouseX, pMouseY) && ((GuiEventListener)x).mouseScrolled(pMouseX, pMouseY, pDelta) && !b[0]) {
                b[0] = true;
            }
        });
        super.mouseScrolled(pMouseX, pMouseY, pDelta);
        return b[0];
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        boolean[] b = new boolean[] { false };
        this.renderables.stream().filter(x -> x instanceof GuiEventListener).forEach(x -> {
            if (((GuiEventListener)x).mouseReleased(pMouseX, pMouseY, pButton) && !b[0]) {
                b[0] = true;
            }
        });
        super.mouseReleased(pMouseX, pMouseY, pButton);
        return b[0];
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
                groupCreatePattern.performForEach(x -> x instanceof IconButton, w -> ((IconButton)w).deselect());
                break;
            case EDITOR:
                pixels = new int[TrafficSignShape.MAX_WIDTH][];
                for (int a = 0; a < pixels.length; a++) {
                    pixels[a] = new int[TrafficSignShape.MAX_HEIGHT];
                }
                name = new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.pattern.name_unknown").getString();
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
        OPEN(10);

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
    }
}

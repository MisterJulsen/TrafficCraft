package de.mrjulsen.trafficcraft.screen;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.screen.widgets.IconButton.ButtonType;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer;
import de.mrjulsen.trafficcraft.screen.widgets.ControlCollection;
import de.mrjulsen.trafficcraft.screen.widgets.HScrollBar;
import de.mrjulsen.trafficcraft.screen.widgets.ICustomAreaControl;
import de.mrjulsen.trafficcraft.screen.widgets.IconButton;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer.BrownAreaStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class  TrafficSignWorkbenchGui extends AbstractContainerScreen<TrafficSignWorkbenchMenu> {

    public static final Component title = new TextComponent("trafficsignworkbench");

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int WIDTH = 230;
    private static final int HEIGHT = 256;

    private static final int MAX_ENTRIES_IN_ROW = 4;
    private static final int MAX_ROWS = 3;

    // Groups
    private final ControlCollection groupDefaultModeButtons = new ControlCollection();

    private final ControlCollection groupToolbar1 = new ControlCollection();
    private final ControlCollection groupToolbar2 = new ControlCollection();
    private final ControlCollection groupShapes = new ControlCollection();
    private final ControlCollection groupColors = new ControlCollection();
    private final ControlCollection groupPatterns = new ControlCollection();
      
    private int guiLeft;
    private int guiTop;

    private BlockPos blockPos;
    private Level level;
    private Player player;

    private final TranslatableComponent textTitle = new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.title");

    private final Component[] defaultModeButtonsTooltips = new Component[] {
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.menu.add"),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.menu.edit"),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.menu.delete")
    };

    private final Component[] toolbar1Tooltips = new Component[] {
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.toolbar.draw"),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.toolbar.erase"),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.toolbar.pick_color"),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.toolbar.text")
    };

    private final Component[] toolbar2Tooltips = new Component[] {
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.toolbar.background"),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.toolbar.clear"),
        new TranslatableComponent("gui.trafficcraft.trafficsignworkbench.toolbar.load")
    };

    private HScrollBar shapesScrollBar;

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
        return true;
    }

    @Override
    public void init() {
        super.init();
        guiLeft = this.width / 2 - WIDTH / 2;
        guiTop = this.height / 2 - HEIGHT / 2;

        /* DEFAULT MODE CONTROLS */        
        // Add new
        this.addRenderableWidget(new IconButton(ButtonType.DEFAULT, groupDefaultModeButtons, guiLeft + 9, guiTop + 36 + 0 * IconButton.HEIGHT, playerInventoryTitle, (btn) -> {
            // click action
        }, (pButton, pPoseStack, pMouseX, pMouseY) -> {
            this.renderComponentTooltip(pPoseStack, List.of(defaultModeButtonsTooltips[0]), pMouseX, pMouseY);
        }) {
            @Override
            protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                ButtonIcons.ADD.render(pPoseStack, x, y);
            }
        });
        
        // Edit selected
        this.addRenderableWidget(new IconButton(ButtonType.DEFAULT, groupDefaultModeButtons, guiLeft + 9, guiTop + 36 + 1 * IconButton.HEIGHT, playerInventoryTitle, (btn) -> {
            // click action
        }, (pButton, pPoseStack, pMouseX, pMouseY) -> {
            this.renderComponentTooltip(pPoseStack, List.of(defaultModeButtonsTooltips[1]), pMouseX, pMouseY);
        }) {
            @Override
            protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                ButtonIcons.EDIT.render(pPoseStack, x, y);
            }
        });

        // Delete selected
        this.addRenderableWidget(new IconButton(ButtonType.DEFAULT, groupDefaultModeButtons, guiLeft + 9, guiTop + 36 + 2 * IconButton.HEIGHT, playerInventoryTitle, (btn) -> {
            // click action
        }, (pButton, pPoseStack, pMouseX, pMouseY) -> {
            this.renderComponentTooltip(pPoseStack, List.of(defaultModeButtonsTooltips[2]), pMouseX, pMouseY);
        }) {
            @Override
            protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                ButtonIcons.DELETE.render(pPoseStack, x, y);
            }
        });
        // #######################

        /*
        // Toolbar 1
        for (int i = 0; i < 4; i++) {  
            final int j = i;          
            this.addRenderableWidget(new IconButton(ButtonType.RADIO_BUTTON, groupToolbar1, guiLeft + 145, guiTop + 30 + i * IconButton.HEIGHT, playerInventoryTitle, (btn) -> {

            }, (pButton, pPoseStack, pMouseX, pMouseY) -> {
                this.renderComponentTooltip(pPoseStack, List.of(toolbar1Tooltips[j]), pMouseX, pMouseY);
            }) {
                @Override
                protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                    super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                    RenderSystem.setShaderTexture(0, gui);
                    blit(pPoseStack, x + 0, y + 0, 265, 18 * (j + 1), WIDTH, HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                }
            });
        }        

        // Toolbar 2
        for (int i = 0; i < 3; i++) { 
            final int j = i;
            this.addRenderableWidget(new IconButton(ButtonType.DEFAULT, groupToolbar2, guiLeft + 145, guiTop + 104 + i * IconButton.HEIGHT, playerInventoryTitle, (btn) -> {
                
            }, (pButton, pPoseStack, pMouseX, pMouseY) -> {
                this.renderComponentTooltip(pPoseStack, List.of(toolbar2Tooltips[j]), pMouseX, pMouseY);
            }) {
                @Override
                protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                    super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                    RenderSystem.setShaderTexture(0, gui);
                    blit(pPoseStack, x + 0, y + 0, 265, 18 * (j + 5), WIDTH, HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                }
            });
        }

        // Shapes
        final int x = guiLeft + 176;
        final int y = guiTop + 30;
        final IconButton[] shapeButtons = Arrays.stream(TrafficSignShape.values()).map(pShape -> {
            final TrafficSignShape shape = pShape;
            IconButton button = new IconButton(ButtonType.RADIO_BUTTON, groupShapes, x, y, playerInventoryTitle, (btn) -> {

            }, (pButton, pPoseStack, pMouseX, pMouseY) -> {
                this.renderComponentTooltip(pPoseStack, List.of(new TranslatableComponent(shape.getTranslationKey())), pMouseX, pMouseY);
            }) {
                @Override
                protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                    super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                    RenderSystem.setShaderTexture(0, shape.getIconResourceLocation());
                    blit(pPoseStack, x + 1, y + 1, 0, 0, 16, 16, 16, 16);
                }
            };
            return this.addRenderableWidget(button);
        }).toArray(IconButton[]::new);

        this.shapesScrollBar = this.addRenderableWidget(new HScrollBar(guiLeft + 250, guiTop + 29, 8, 56, new GuiAreaDefinition(guiLeft + 175, guiTop + 29, 74, 56)).setOnValueChangedEvent((scrollBar) -> {
            fillButtons(shapeButtons, scrollBar.getScrollValue(), x, y, scrollBar);
        }));       
        fillButtons(shapeButtons, 0, x, y, shapesScrollBar);
        */
    }

    private void fillButtons(IconButton[] buttons, int scrollRow, int defX, int defY, HScrollBar scrollbar) {
        int currentRow = -1;
        for (int i = 0; i < buttons.length; i++) {
            if (i % MAX_ENTRIES_IN_ROW == 0)
                currentRow++;

            buttons[i].x = defX + (i % MAX_ENTRIES_IN_ROW) * IconButton.WIDTH;
            buttons[i].y = defY + (currentRow) * IconButton.HEIGHT - (scrollRow * IconButton.HEIGHT);
            buttons[i].visible = currentRow >= scrollRow && currentRow < scrollRow + MAX_ROWS;
        }
        scrollbar.setMaxRowsOnPage(MAX_ROWS).updateMaxScroll(currentRow + 1);
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
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        blit(pPoseStack, guiLeft, guiTop, 0, 0, WIDTH, HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        if (this.getMenu().patternSlot.hasItem()) {
            RenderSystem.setShaderTexture(0, OVERLAY);
            blit(pPoseStack, guiLeft + 36, guiTop + 14, 0, 0, 158, 174, 256, 256);

            // render arrow
            blit(pPoseStack, guiLeft + 51, guiTop + 164, isMouseInBounds(guiLeft + 51, guiTop + 164, 23, 13, pMouseX, pMouseY) ? 23 : 0, 174 + 13, 23, 13, 256, 256); //right
            blit(pPoseStack, guiLeft + 149, guiTop + 164, isMouseInBounds(guiLeft + 149, guiTop + 164, 23, 13, pMouseX, pMouseY) ? 23 : 0, 174, 23, 13, 256, 256); //left
            
            // render pattern count            
            String label = String.format("%s / %s", 0, PatternCatalogueItem.getStoredPatternCount(this.getMenu().patternSlot.getItem()));
            this.font.draw(pPoseStack, label, guiLeft + WIDTH / 2 - font.width(label) / 2, guiTop + 170 - font.lineHeight / 2, 4210752);

            // render menu buttons
            AreaRenderer.renderBrownArea(pPoseStack, guiLeft + 8, guiTop + 35, 20, 56, BrownAreaStyle.SUNKEN);
        }
        
        groupDefaultModeButtons.setVisible(this.getMenu().patternSlot.hasItem());
        
        this.font.draw(pPoseStack, textTitle.getString(), guiLeft + WIDTH / 2 - font.width(textTitle) / 2, guiTop + 5, 4210752);
        
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack, pMouseY);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pPoseStack, pMouseX, pMouseY);        
        
        this.renderables.stream().filter(x -> x instanceof AbstractWidget).forEach(x -> {
            AbstractWidget w = (AbstractWidget)x;
            if (w.isHoveredOrFocused()) {
                w.renderToolTip(pPoseStack, pMouseX, pMouseY);
            }
        });
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
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

    private boolean isMouseInBounds(int x, int y, int w, int h, int mX, int mY) {
        return mX >= x && mX <= x + w && mY >= y && mY <= y + h;
    }


    protected enum TrafficSignWorkbenchMode {
        EMPTY,
        DEFAULT,
        CREATE_NEW,
        EDITOR
    }

    protected enum ButtonIcons {
        EDIT(0),
        ERASER(1),
        PICK_COLOR(2),
        TEXT(3),
        FILL(4),
        DELETE(5),
        PATTERN(6),
        ADD(7);

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

        public void render(PoseStack pPoseStack, int x, int y) {   
            RenderSystem.setShaderTexture(0, OVERLAY);         
            blit(pPoseStack, x, y, this.getU(), this.getV(), ICON_SIZE, ICON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }
}

package de.mrjulsen.trafficcraft.client.screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

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
import de.mrjulsen.mcdragonlib.client.gui.wrapper.CommonScreen;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.client.TrafficSignTextureCacheClient;
import de.mrjulsen.trafficcraft.data.TrafficSignData;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.PatternCatalogueIndexPacket;
import de.mrjulsen.trafficcraft.network.packets.CreativePatternCataloguePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrafficSignPatternSelectionScreen extends CommonScreen {
    
    public static final Component title = GuiUtils.translate("gui.trafficcraft.patternselection.title");
    
    private static final int WIDTH = 158;
    private static final int HEIGHT = 200;
    
    private static final int TEXTURE_WIDTH = 158;
    private static final int TEXTURE_HEIGHT = 174;

    private static final int MAX_ENTRIES_IN_ROW = 6;
    private static final int MAX_ROWS = 6;

    private static final int ICON_BUTTON_WIDTH = 18;
    private static final int ICON_BUTTON_HEIGHT = 18;
    
    private static final int BOOKMARK_U = WIDTH;
    private static final int BOOKMARK_V_UNSELECTED_LEFT = 0;
    private static final int BOOKMARK_V_SELECTED_LEFT = 20;
    private static final int BOOKMARK_V_UNSELECTED_RIGHT = 40;
    private static final int BOOKMARK_V_SELECTED_RIGHT = 60;
    private static final int BOOKMARK_HEIGHT = 20;
    private static final int BOOKMARK_WIDTH = 44;
    private static final int BOOKMARK_SPACING = 2;
    private static final int BOOKMARK_COUNT_PER_SIDE = 6;
    
    private static final int BOOKMARK_Y_START = 17;
    private static final int BOOKMARK_X_LEFT = -33;
    private static final int BOOKMARK_X_RIGHT = WIDTH - 16;
    
    private final WidgetsCollection groupPatterns = new WidgetsCollection();
    private VerticalScrollBar scrollbar;

    private int guiTop;
    private int guiLeft;

    // bookmarks
    private final TrafficSignShape[] bookmarks = new TrafficSignShape[] {
        TrafficSignShape.CIRCLE,
        TrafficSignShape.TRIANGLE,
        TrafficSignShape.SQUARE,
        TrafficSignShape.DIAMOND,
        TrafficSignShape.RECTANGLE,
        TrafficSignShape.MISC,
    };
    private int selectedBookmark = bookmarks.length;
    private int scroll = 0;
    private int selectedIndex;

    private final ItemStack stack;
    private final boolean creative;
    
    private static final ResourceLocation OVERLAY = new ResourceLocation(ModMain.MOD_ID, "textures/gui/traffic_sign_workbench_overlay.png");

    public TrafficSignPatternSelectionScreen(ItemStack stack) {
        super(title);

        if (!(stack.getItem() instanceof PatternCatalogueItem)) {
            throw new IllegalStateException("ItemStack is no PatternCatalogueItem.");
        }

        this.stack = stack;
        this.creative = stack.getItem() instanceof CreativePatternCatalogueItem;
        selectedBookmark = stack.getItem() instanceof CreativePatternCatalogueItem && CreativePatternCatalogueItem.shouldUseCustomPattern(stack) ? 0 : bookmarks.length;
        this.selectedIndex = PatternCatalogueItem.getSelectedIndex(stack);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        if (selectedBookmark >= bookmarks.length) {
            NetworkManager.MOD_CHANNEL.sendToServer(new PatternCatalogueIndexPacket(PatternCatalogueItem.getSelectedIndex(stack)));
        } else {
            TrafficSignData data = CreativePatternCatalogueItem.getCustomImage(stack);
            if (data != null) {                
                NetworkManager.MOD_CHANNEL.sendToServer(new CreativePatternCataloguePacket(data));
                data.close();
            }
        }
        super.onClose();
    }

    @Override
    public void init() {
        super.init();
        
        guiLeft = this.width / 2 - WIDTH / 2;
        guiTop = this.height / 2 - HEIGHT / 2;
        scroll = 0;

        groupPatterns.components.clear();

        if (selectedBookmark >= bookmarks.length) {
            final int count = PatternCatalogueItem.getStoredPatternCount(stack);
            for (int i = 0; i < count; i++) {
                final int j = i;

                IconButton btn = new IconButton(ButtonType.RADIO_BUTTON, AreaStyle.BROWN, Sprite.empty(), groupPatterns, guiLeft + 9, guiTop + 36 + j * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, null, (button) -> {
                PatternCatalogueItem.setSelectedIndex(stack, j);
                if (stack.getItem() instanceof CreativePatternCatalogueItem) {
                    CreativePatternCatalogueItem.clearCustomImage(stack);
                }
                }) {
                    @Override
                    protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                        super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                        try (TrafficSignData data = PatternCatalogueItem.getPatternAt(stack, j)) {
                            DynamicTexture tex = TrafficSignTextureCacheClient.getTexture(data, data.getTexture(), false, (t) -> {
                                data.setFromBase64(TrafficSignTextureCacheClient.textureToBase64(data));
                            });
                            RenderSystem.setShaderTexture(0, tex.getId());
                            NativeImage img = tex.getPixels();
                            blit(pPoseStack, x + 1, y + 1, ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2, 0, 0, img.getWidth(), img.getHeight(), img.getWidth(), img.getHeight());
                        }
                        
                    }
                }.withAlignment(Alignment.CENTER);
                addTooltip(Tooltip.of(GuiUtils.text(PatternCatalogueItem.getPatternAt(stack, j).getName())).assignedTo(btn));
                this.addRenderableWidget(btn);
            }
        } else {
            // builtin textures
            final TrafficSignShape[] shapes = bookmarks[selectedBookmark] == TrafficSignShape.MISC ? Arrays.stream(TrafficSignShape.values()).filter(x -> {
                return !Arrays.stream(bookmarks).anyMatch(y -> x == y) || x == TrafficSignShape.MISC;
            }).toArray(TrafficSignShape[]::new) : new TrafficSignShape[] { bookmarks[selectedBookmark] };

            for (TrafficSignShape shape : shapes) {
                int a = 1;
                ResourceLocation path = new ResourceLocation(ModMain.MOD_ID + ":" + "textures/block/sign/" + shape.getShape() + "/" + shape.getShape() + a + ".png");
                List<ResourceLocation> locs = new ArrayList<>();
                while (Minecraft.getInstance().getResourceManager().hasResource(path)) {
                    locs.add(path);
                    a++;
                    path = new ResourceLocation(ModMain.MOD_ID + ":" + "textures/block/sign/" + shape.getShape() + "/" + shape.getShape() + a + ".png");
                }
                final ResourceLocation[] resources = locs.toArray(ResourceLocation[]::new);
                final int count = resources.length;

                for (int i = 0; i < count; i++) {
                    final int j = i;
                    Sprite sprite = new Sprite(resources[j], 32, 32, 0, 0, 32, 32, ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2);

                    IconButton btn = new IconButton(ButtonType.RADIO_BUTTON, AreaStyle.BROWN, sprite, groupPatterns, guiLeft + 9, guiTop + 36 + j * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, null, (button) -> {
                        try (NativeImage img = NativeImage.read(this.minecraft.getResourceManager().getResource(resources[j]).getInputStream())) {
                            TrafficSignData tsd = new TrafficSignData(img.getWidth(), img.getHeight(), shape);
                            tsd.setFromBase64(TrafficSignTextureCacheClient.textureToBase64(img));
                            CreativePatternCatalogueItem.setCustomImage(stack, tsd);
                            selectedIndex = j;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).withAlignment(Alignment.CENTER);
                    this.addRenderableWidget(btn);
                }
            }
        }
        
        this.scrollbar = this.addRenderableWidget(new VerticalScrollBar(guiLeft + WIDTH / 2 + ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2, guiTop + 45 - 1, 8, MAX_ROWS * ICON_BUTTON_HEIGHT + 2,
            new GuiAreaDefinition(
                guiLeft + WIDTH / 2 - ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2,
                guiTop + 45,
                MAX_ENTRIES_IN_ROW * ICON_BUTTON_WIDTH,
                MAX_ROWS * ICON_BUTTON_HEIGHT
            )
        )).setAutoScrollerHeight(true).setOnValueChangedEvent((scrollbar) -> {
            this.scroll = scrollbar.getScrollValue();
            fillButtons(groupPatterns.components.toArray(IconButton[]::new), scroll, guiLeft + WIDTH / 2 - ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2 - 1, guiTop + 45, this.scrollbar);
        });

        this.scrollbar.visible = groupPatterns.components.size() > MAX_ENTRIES_IN_ROW * MAX_ROWS;
        fillButtons(groupPatterns.components.toArray(IconButton[]::new), scroll, guiLeft + WIDTH / 2 - ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2 - 1, guiTop + 45, this.scrollbar);
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
    public void render(PoseStack pPoseStack, int mouseX, int mouseY, float partialTicks) {        
        renderBackground(pPoseStack, 0);
        
        GuiUtils.blit(OVERLAY, pPoseStack, guiLeft, guiTop + 26, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, 256, 256);
        
        if (creative) {
            int bookY = guiTop + 26;
            int bookmarkIndex = 0;
            for (TrafficSignShape shape : bookmarks) {      
                bookmarkIndex = addBookmark(pPoseStack, mouseX, mouseY, partialTicks, bookY, bookmarkIndex, shape.getIconResourceLocation(), 0, 0, 32, 32, 32, 32);
            }
            // Bookmark custom textures
            bookmarkIndex = addBookmark(pPoseStack, mouseX, mouseY, partialTicks, bookY, bookmarkIndex, OVERLAY, 239, 0, 16, 16, 256, 256);
        }

        DynamicGuiRenderer.renderArea(pPoseStack, guiLeft + WIDTH / 2 - ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2 - 2, guiTop + 45 - 1, MAX_ENTRIES_IN_ROW * ICON_BUTTON_WIDTH + 2, MAX_ROWS * ICON_BUTTON_HEIGHT + 2, AreaStyle.BROWN, ButtonState.SUNKEN);

        if (CreativePatternCatalogueItem.hasCustomPattern(stack)) {            
            try (TrafficSignData data = CreativePatternCatalogueItem.getCustomImage(stack)) {
                DynamicTexture tex = TrafficSignTextureCacheClient.getTexture(data, data.getTexture(), false, (t) -> {
                    data.setFromBase64(TrafficSignTextureCacheClient.textureToBase64(data));
                });
                NativeImage img = tex.getPixels();
                GuiUtils.blit(tex.getId(), pPoseStack, guiLeft + 15, guiTop + HEIGHT - 15 - 24, 24, 24, 0, 0, img.getWidth(), img.getHeight(), img.getWidth(), img.getHeight());
                img.close();

                float scale = 0.75f;
                pPoseStack.scale(scale, scale, scale);
                this.font.draw(pPoseStack, GuiUtils.translate("gui.trafficcraft.patternselection.build_in_pattern", GuiUtils.translate(data.getShape().getTranslationKey()).getString(), selectedIndex + 1), (guiLeft + 15 + 30) / scale, (guiTop + HEIGHT - 15 - 24 / 2 - this.font.lineHeight / 2) / scale, 4210752);
                pPoseStack.setIdentity();
            }
            
        } else {
            try (TrafficSignData data = PatternCatalogueItem.getSelectedPattern(stack)) {
                if (data != null) {
                    DynamicTexture tex = TrafficSignTextureCacheClient.getTexture(data, data.getTexture(), false, (t) -> {
                        data.setFromBase64(TrafficSignTextureCacheClient.textureToBase64(data));
                    });
                    NativeImage img = tex.getPixels();
                    GuiUtils.blit(tex.getId(), pPoseStack, guiLeft + 15, guiTop + HEIGHT - 15 - 24, 24, 24, 0, 0, img.getWidth(), img.getHeight(), img.getWidth(), img.getHeight());
                    img.close();

                    float scale = 0.75f;
                    pPoseStack.scale(scale, scale, scale);
                    this.font.draw(pPoseStack, data.getName(), (guiLeft + 15 + 30) / scale, (guiTop + HEIGHT - 15 - 24 / 2 - this.font.lineHeight / 2) / scale, 4210752);
                    pPoseStack.setIdentity();
                }
            }            
        }

        drawCenteredString(pPoseStack, this.font, title, this.width / 2, guiTop, 16777215);        
        
        super.render(pPoseStack, mouseX, mouseY, partialTicks);
        groupPatterns.performForEach(x -> x.renderToolTip(pPoseStack, mouseX, mouseY));
    }

    private int addBookmark(PoseStack pPoseStack, int mouseX, int mouseY, float partialTicks, int bookY, int bookmarkIndex, ResourceLocation icon, int u, int v, int uW, int vH, int texW, int texH) {
        int idx = bookmarkIndex % BOOKMARK_COUNT_PER_SIDE;
        int bookmarkX = bookmarkIndex / BOOKMARK_COUNT_PER_SIDE <= 0 ? BOOKMARK_X_LEFT : BOOKMARK_X_RIGHT;
        int bookmarkV = bookmarkIndex / BOOKMARK_COUNT_PER_SIDE <= 0 ? (selectedBookmark == bookmarkIndex ? BOOKMARK_V_SELECTED_LEFT : BOOKMARK_V_UNSELECTED_LEFT) : (selectedBookmark == bookmarkIndex ? BOOKMARK_V_SELECTED_RIGHT : BOOKMARK_V_UNSELECTED_RIGHT);
        GuiUtils.blit(OVERLAY, pPoseStack, 
            guiLeft + bookmarkX,
            bookY + BOOKMARK_Y_START + idx * (BOOKMARK_HEIGHT + BOOKMARK_SPACING),
            BOOKMARK_WIDTH,
            BOOKMARK_HEIGHT,
            BOOKMARK_U,
            bookmarkV,
            BOOKMARK_WIDTH,
            BOOKMARK_HEIGHT,
            256,
            256
        );
        GuiUtils.blit(icon, pPoseStack, 
            guiLeft + bookmarkX + 14,
            bookY + BOOKMARK_Y_START + idx * (BOOKMARK_HEIGHT + BOOKMARK_SPACING) + 2,
            16,
            16,
            u,
            v,
            uW,
            vH,
            texW,
            texH
        );

        bookmarkIndex++;

        return bookmarkIndex;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {  
        if (creative) {
            int bookY = guiTop + 26;
            int xLeft = guiLeft + BOOKMARK_X_LEFT;
            int xRight = guiLeft + BOOKMARK_X_RIGHT;
            int y = bookY + BOOKMARK_Y_START;
            int h1 = y + Math.min(BOOKMARK_COUNT_PER_SIDE, bookmarks.length + 1) * (BOOKMARK_HEIGHT + BOOKMARK_SPACING);
            int h2 = y + (bookmarks.length - BOOKMARK_COUNT_PER_SIDE + 1) * (BOOKMARK_HEIGHT + BOOKMARK_SPACING);

            //left tabs
            if (pMouseX > xLeft && pMouseX < xLeft + BOOKMARK_WIDTH && pMouseY > y && pMouseY < h1) {
                int index = Mth.clamp((int)((pMouseY - y) / (BOOKMARK_HEIGHT + BOOKMARK_SPACING)), 0, BOOKMARK_COUNT_PER_SIDE);
                this.selectedBookmark = index;
                this.clearWidgets();
                init();
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.2F));
            }

            //right tabs
            if (bookmarks.length + 1 > BOOKMARK_COUNT_PER_SIDE && pMouseX > xRight && pMouseX < xRight + BOOKMARK_WIDTH && pMouseY > y && pMouseY < h2) {
                int index = BOOKMARK_COUNT_PER_SIDE + Mth.clamp((int)((pMouseY - y) / (BOOKMARK_HEIGHT + BOOKMARK_SPACING)), 0, BOOKMARK_COUNT_PER_SIDE);
                this.selectedBookmark = index;
                this.clearWidgets();
                init();
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.2F));
            }
        }
              
        return super.mouseClicked(pMouseX, pMouseY, pButton);
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

    /*
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
    */
}

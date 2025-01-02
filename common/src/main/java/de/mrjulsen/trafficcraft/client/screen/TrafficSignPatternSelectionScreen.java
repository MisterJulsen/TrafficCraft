package de.mrjulsen.trafficcraft.client.screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLIconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
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
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.data.TrafficSignClientTexture;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference.BuildInTrafficSignCodec;
import de.mrjulsen.trafficcraft.data.TrafficSignTextureMetadata;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.packets.cts.CreativePatternCataloguePacket;
import de.mrjulsen.trafficcraft.network.packets.cts.PatternCatalogueIndexPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class TrafficSignPatternSelectionScreen extends DLScreen {
    
    public static final Component title = TextUtils.translate("gui.trafficcraft.patternselection.title");
    
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
    private DLVerticalScrollBar scrollbar;
    private boolean updateScrollableContent = true;

    private int guiTop;
    private int guiLeft;

    private final Map<NamedTrafficSignTextureReference, TrafficSignClientTexture> cachedTextures = new HashMap<>();

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
    private double scroll = 0;
    private int selectedIndex;

    private final ItemStack stack;
    private final boolean creative;
    
    private static final ResourceLocation OVERLAY = new ResourceLocation(TrafficCraft.MOD_ID, "textures/gui/traffic_sign_workbench_overlay.png");

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
            TrafficCraft.net().sendToServer(new PatternCatalogueIndexPacket(PatternCatalogueItem.getSelectedIndex(stack)));
        } else {NamedTrafficSignTextureReference data = CreativePatternCatalogueItem.getCustomImage(stack);
            if (data != null) {
                TrafficCraft.net().sendToServer(new CreativePatternCataloguePacket(data));
            }
        }
        cachedTextures.values().forEach(x -> x.close());
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

                DLIconButton btn = new DLIconButton(ButtonType.RADIO_BUTTON, AreaStyle.BROWN, Sprite.empty(), groupPatterns, guiLeft + 9, guiTop + 36 + j * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, null, (button) -> {
                PatternCatalogueItem.setSelectedIndex(stack, j);
                if (stack.getItem() instanceof CreativePatternCatalogueItem) {
                    CreativePatternCatalogueItem.clearCustomImage(stack);
                }
                }) {
                    public void renderImage(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
                        NamedTrafficSignTextureReference data = PatternCatalogueItem.getPatternAt(stack, j);
                        TrafficSignClientTexture tex = cachedTextures.computeIfAbsent(data, x -> TrafficSignClientTexture.load(data.getTextureId(), false));
                        GuiUtils.drawTexture(tex.getTextureLocation(), graphics, x + 1, y + 1, ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2, 0, 0, tex.getRawData().getWidth(), tex.getRawData().getHeight(), tex.getRawData().getWidth(), tex.getRawData().getHeight());
                    }
                }.withAlignment(EAlignment.CENTER);
                addTooltip(DLTooltip.of(TextUtils.text(PatternCatalogueItem.getPatternAt(stack, j).getName())).assignedTo(btn));
                this.addRenderableWidget(btn);
            }
        } else {
            // builtin textures
            final TrafficSignShape[] shapes = bookmarks[selectedBookmark] == TrafficSignShape.MISC ? Arrays.stream(TrafficSignShape.values()).filter(x -> {
                return !Arrays.stream(bookmarks).anyMatch(y -> x == y) || x == TrafficSignShape.MISC;
            }).toArray(TrafficSignShape[]::new) : new TrafficSignShape[] { bookmarks[selectedBookmark] };

            for (TrafficSignShape shape : shapes) {
                int a = 1;
                ResourceLocation path = new ResourceLocation(TrafficCraft.MOD_ID + ":" + "textures/block/sign/" + shape.getShape() + "/" + shape.getShape() + a + ".png");
                List<TrafficSignTextureMetadata> locs = new ArrayList<>();
                while (Minecraft.getInstance().getResourceManager().hasResource(path)) {
                    short width = 32;
                    short height = 32;
                    try (NativeImage img = NativeImage.read(this.minecraft.getResourceManager().getResource(path).getInputStream())) {
                        width = (short)img.getWidth();
                        height = (short)img.getHeight();
                    } catch (IOException e) {
                        TrafficCraft.LOGGER.warn("Unable to determine texture size.", e);
                    }
                    locs.add(new TrafficSignTextureMetadata(path, shape, a, width, height));

                    a++;
                    path = new ResourceLocation(TrafficCraft.MOD_ID + ":" + "textures/block/sign/" + shape.getShape() + "/" + shape.getShape() + a + ".png");
                }

                final int count = locs.size();
                for (int i = 0; i < count; i++) {
                    final int j = i;
                    Sprite sprite = new Sprite(locs.get(j).location(), 32, 32, 0, 0, 32, 32, ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2);

                    DLIconButton btn = new DLIconButton(ButtonType.RADIO_BUTTON, AreaStyle.BROWN, sprite, groupPatterns, guiLeft + 9, guiTop + 36 + j * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, null, (button) -> {
                        CreativePatternCatalogueItem.setCustomImage(stack, NamedTrafficSignTextureReference.ofBuildIn("", new BuildInTrafficSignCodec(locs.get(j).shape(), locs.get(j).id(), locs.get(j).width(), locs.get(j).height())));
                        selectedIndex = j;
                    }).withAlignment(EAlignment.CENTER);
                    this.addRenderableWidget(btn);
                }
            }
        }
        
        this.scrollbar = this.addRenderableWidget(new DLVerticalScrollBar(guiLeft + WIDTH / 2 + ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2, guiTop + 45 - 1, 8, MAX_ROWS * ICON_BUTTON_HEIGHT + 2,
            new GuiAreaDefinition(
                guiLeft + WIDTH / 2 - ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2,
                guiTop + 45,
                MAX_ENTRIES_IN_ROW * ICON_BUTTON_WIDTH,
                MAX_ROWS * ICON_BUTTON_HEIGHT
            )
        )).setAutoScrollerSize(true).withOnValueChanged((scrollbar) -> {
            this.scroll = scrollbar.getScrollValue();
            if (updateScrollableContent)
                fillButtons(groupPatterns.components.toArray(DLIconButton[]::new), scroll, guiLeft + WIDTH / 2 - ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2 - 1, guiTop + 45, this.scrollbar);

            updateScrollableContent = true;
        });

        this.scrollbar.visible = groupPatterns.components.size() > MAX_ENTRIES_IN_ROW * MAX_ROWS;
        fillButtons(groupPatterns.components.toArray(DLIconButton[]::new), scroll, guiLeft + WIDTH / 2 - ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2 - 1, guiTop + 45, this.scrollbar);
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

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {        
        renderScreenBackground(graphics);
        
        GuiUtils.drawTexture(OVERLAY, graphics, guiLeft, guiTop + 26, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, 256, 256);
        
        if (creative) {
            int bookY = guiTop + 26;
            int bookmarkIndex = 0;
            for (TrafficSignShape shape : bookmarks) {      
                bookmarkIndex = addBookmark(graphics, mouseX, mouseY, partialTicks, bookY, bookmarkIndex, shape.getIconResourceLocation(), 0, 0, 32, 32, 32, 32);
            }
            // Bookmark custom textures
            bookmarkIndex = addBookmark(graphics, mouseX, mouseY, partialTicks, bookY, bookmarkIndex, OVERLAY, 239, 0, 16, 16, 256, 256);
        }

        DynamicGuiRenderer.renderArea(graphics, guiLeft + WIDTH / 2 - ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2 - 2, guiTop + 45 - 1, MAX_ENTRIES_IN_ROW * ICON_BUTTON_WIDTH + 2, MAX_ROWS * ICON_BUTTON_HEIGHT + 2, AreaStyle.BROWN, ButtonState.DOWN);

        if (CreativePatternCatalogueItem.hasCustomPattern(stack)) {            
            NamedTrafficSignTextureReference data = CreativePatternCatalogueItem.getCustomImage(stack);
            TrafficSignClientTexture tex = cachedTextures.computeIfAbsent(data, x -> TrafficSignClientTexture.load(data.getTextureId(), false));
            BuildInTrafficSignCodec codec = BuildInTrafficSignCodec.decode(data.getTextureId());
            GuiUtils.drawTexture(tex.getTextureLocation(), graphics, guiLeft + 15, guiTop + HEIGHT - 15 - 24, 24, 24, 0, 0, tex.getRawData().getWidth(), tex.getRawData().getHeight(), tex.getRawData().getWidth(), tex.getRawData().getHeight());

            float scale = 0.75f;
            graphics.poseStack().pushPose();
            graphics.poseStack().scale(scale, scale, scale);
            GuiUtils.drawString(graphics, font, (int)((guiLeft + 15 + 30) / scale), (int)((guiTop + HEIGHT - 15 - 24 / 2 - this.font.lineHeight / 2) / scale), TextUtils.translate("gui.trafficcraft.patternselection.build_in_pattern", TextUtils.translate(codec.shape().getTranslationKey()).getString(), selectedIndex + 1), DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
            graphics.poseStack().popPose();
            
        } else {
            NamedTrafficSignTextureReference data = PatternCatalogueItem.getSelectedPattern(stack);
            if (data != null) {
                TrafficSignClientTexture tex = cachedTextures.computeIfAbsent(data, x -> TrafficSignClientTexture.load(data.getTextureId(), false));
                GuiUtils.drawTexture(tex.getTextureLocation(), graphics, guiLeft + 15, guiTop + HEIGHT - 15 - 24, 24, 24, 0, 0, tex.getRawData().getWidth(), tex.getRawData().getHeight(), tex.getRawData().getWidth(), tex.getRawData().getHeight());

                float scale = 0.75f;
                graphics.poseStack().pushPose();
                graphics.poseStack().scale(scale, scale, scale);
                GuiUtils.drawString(graphics, font, (int)((guiLeft + 15 + 30) / scale), (int)((guiTop + HEIGHT - 15 - 24 / 2 - this.font.lineHeight / 2) / scale), data.getName(), DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
                graphics.poseStack().popPose();
            }
        }

        GuiUtils.drawString(graphics, this.font, this.width / 2, guiTop, title, DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.CENTER, false);        
        
        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderFrontLayer(graphics, mouseX, mouseY, partialTick);        
        groupPatterns.performForEach(x -> x.renderToolTip(graphics.poseStack(), mouseX, mouseY));
    }

    private int addBookmark(Graphics graphics, int mouseX, int mouseY, float partialTicks, int bookY, int bookmarkIndex, ResourceLocation icon, int u, int v, int uW, int vH, int texW, int texH) {
        int idx = bookmarkIndex % BOOKMARK_COUNT_PER_SIDE;
        int bookmarkX = bookmarkIndex / BOOKMARK_COUNT_PER_SIDE <= 0 ? BOOKMARK_X_LEFT : BOOKMARK_X_RIGHT;
        int bookmarkV = bookmarkIndex / BOOKMARK_COUNT_PER_SIDE <= 0 ? (selectedBookmark == bookmarkIndex ? BOOKMARK_V_SELECTED_LEFT : BOOKMARK_V_UNSELECTED_LEFT) : (selectedBookmark == bookmarkIndex ? BOOKMARK_V_SELECTED_RIGHT : BOOKMARK_V_UNSELECTED_RIGHT);
        GuiUtils.drawTexture(OVERLAY, graphics, 
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
        GuiUtils.drawTexture(icon, graphics, 
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
}

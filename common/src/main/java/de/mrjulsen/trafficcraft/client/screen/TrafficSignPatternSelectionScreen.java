package de.mrjulsen.trafficcraft.client.screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLToggleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
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
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.client.ModGuiIcons;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.data.TrafficSignClientTexture;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference.BuildInTrafficSignCodec;
import de.mrjulsen.trafficcraft.data.TrafficSignTextureMetadata;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.packets.cts.CreativePatternCataloguePacket;
import de.mrjulsen.trafficcraft.network.packets.cts.PatternCatalogueIndexPacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TrafficSignPatternSelectionScreen extends DLWindow {
    
    public static final Component title = TextUtils.translate("gui.trafficcraft.patternselection.title");

    private static final int TEXTURE_WIDTH = 158;
    private static final int TEXTURE_HEIGHT = 174;
    
    private static final int BOOKMARK_U = TEXTURE_WIDTH;
    private static final int BOOKMARK_V_UNSELECTED_LEFT = 0;
    private static final int BOOKMARK_V_SELECTED_LEFT = 20;
    private static final int BOOKMARK_V_UNSELECTED_RIGHT = 40;
    private static final int BOOKMARK_V_SELECTED_RIGHT = 60;
    private static final int BOOKMARK_HEIGHT = 20;
    private static final int BOOKMARK_WIDTH = 44;

    private static final int WIDTH = TEXTURE_WIDTH + BOOKMARK_WIDTH * 2;
    private static final int HEIGHT = 200;    

    private static final int MAX_ENTRIES_IN_ROW = 6;
    private static final int MAX_ROWS = 6;

    private static final int ICON_BUTTON_WIDTH = 18;
    private static final int ICON_BUTTON_HEIGHT = 18;
    
    private static final int BOOKMARK_SPACING = 2;
    
    private static final int BOOKMARK_Y_START = 17;
    
    private final DLPanel groupPatterns;
    private final DLPanel innerPanel;
    private DLScrollBar scrollbar;

    private final Map<NamedTrafficSignTextureReference, TrafficSignClientTexture> cachedTextures = new HashMap<>();

    private final DLPanel leftBookmarksPanel;
    private final DLPanel rightBookmarksPanel;

    // bookmarks
    private final List<TrafficSignShape> bookmarks = List.of(
        TrafficSignShape.CIRCLE,
        TrafficSignShape.TRIANGLE,
        TrafficSignShape.SQUARE,
        TrafficSignShape.DIAMOND,
        TrafficSignShape.RECTANGLE,
        TrafficSignShape.MISC
    );
    private TrafficSignShape selectedShape = null;

    private final ItemStack stack;
    private final boolean creative;
    
    private static final DLTexture OVERLAY = new DLTexture(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/gui/traffic_sign_workbench_overlay.png"), 256, 256);

    public TrafficSignPatternSelectionScreen(DLWindowManager manager, ItemStack stack) {
        super(manager);
        setSize(WIDTH, HEIGHT);
        windowSpawnPosition.set(WindowPosition.CENTER);

        if (!(stack.getItem() instanceof PatternCatalogueItem)) {
            throw new IllegalStateException("ItemStack is no PatternCatalogueItem.");
        }

        this.stack = stack;
        this.creative = stack.getItem() instanceof CreativePatternCatalogueItem;
        //selectedBookmark = stack.getItem() instanceof CreativePatternCatalogueItem && CreativePatternCatalogueItem.shouldUseCustomPattern(stack) ? 0 : bookmarks.size();
        //this.selectedShape = PatternCatalogueItem.getSelectedIndex(stack);

        groupPatterns = addComponent(new DLPanel(width() / 2 - (ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2) / 2 - 6, HEIGHT - TEXTURE_HEIGHT + 20, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_WIDTH * MAX_ROWS + 2));
        groupPatterns.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);
        groupPatterns.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                DefaultGuiTextures.DRAGONLIB_UI.getSprite("button_brown_down").render(e.graphics(), 0, 0, s.width(), s.height());
            }
            return false;
        });

        innerPanel = groupPatterns.addComponent(new DLPanel(1, 1, groupPatterns.width() - 2, groupPatterns.height() - 2));
        FlowLayout layout = new FlowLayout();
        layout.flowDirection.set(Direction.HORIZONTAL);
        layout.wrap.set(true);        
        innerPanel.layout.set(layout);
        innerPanel.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);

        scrollbar = addComponent(new DLScrollBar(groupPatterns.x() + groupPatterns.width(), groupPatterns.y(), 8, groupPatterns.height(), Orientation.VERTICAL));
        scrollbar.anchor.set2(EAlign.BOTTOM, EAlign.TOP, EAlign.RIGHT);
        scrollbar.scrollerSize.set(0);
        scrollbar.screenSize.set(innerPanel.height());
        scrollbar.max.set((int)Math.ceil(1 / MAX_ENTRIES_IN_ROW * ICON_BUTTON_HEIGHT));
        scrollbar.inputConsumptionPolicy.set(c -> true);
        scrollbar.scrollSteps.set(ICON_BUTTON_HEIGHT);
        scrollbar.addEventListener(DLScrollBar.ValueChangedEvent.class, (s, e) -> {
            innerPanel.setScrollOffsetY(e.value());
            return false;
        });
        addEventListener(DLGuiStandardEvents.ScrollEvent.class, scrollbar::invokeEvent);


        FlowLayout bookmarkLayout = new FlowLayout();
        bookmarkLayout.flowDirection.set(Direction.VERTICAL);
        bookmarkLayout.verticalGap.set(BOOKMARK_SPACING);
        bookmarkLayout.wrap.set(false);

        leftBookmarksPanel = addComponent(new DLPanel(width() / 2 - TEXTURE_WIDTH / 2 - BOOKMARK_WIDTH + 11, HEIGHT - TEXTURE_HEIGHT + BOOKMARK_Y_START, BOOKMARK_WIDTH, TEXTURE_HEIGHT));
        rightBookmarksPanel = addComponent(new DLPanel(width() / 2 + TEXTURE_WIDTH / 2 - 15, HEIGHT - TEXTURE_HEIGHT + BOOKMARK_Y_START, BOOKMARK_WIDTH, TEXTURE_HEIGHT));

        if (creative) {
            for (TrafficSignShape shape : bookmarks) {
                final TrafficSignShape fShape = shape;
                Bookmark mark = new Bookmark(new DLSprite(new DLTexture(shape.getIconResourceLocation(), 16, 16), 16, 16), false);
                mark.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                    selectedShape = fShape;
                    leftBookmarksPanel.getComponentsOfType(Bookmark.class, true).forEach(a -> a.selected = a == s);
                    rightBookmarksPanel.getComponentsOfType(Bookmark.class, true).forEach(a -> a.selected = a == s);
                    loadTextures();
                    return false;
                });
                leftBookmarksPanel.addComponent(mark);
            }
            
            Bookmark customMark = new Bookmark(ModGuiIcons.EDIT.getAsSprite(16, 16), true);
            customMark.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                selectedShape = null;
                leftBookmarksPanel.getComponentsOfType(Bookmark.class, true).forEach(a -> a.selected = a == s);
                rightBookmarksPanel.getComponentsOfType(Bookmark.class, true).forEach(a -> a.selected = a == s);
                loadTextures();
                return false;
            });
            customMark.selected = true;
            rightBookmarksPanel.addComponent(customMark);
        }

        leftBookmarksPanel.layout.set(bookmarkLayout);
        rightBookmarksPanel.layout.set(bookmarkLayout);

        loadTextures();
    }

    private void loadTextures() {
        innerPanel.clearComponents();
        if (selectedShape != null && bookmarks.contains(selectedShape)) {
            // builtin textures
            final TrafficSignShape[] shapes = selectedShape == TrafficSignShape.MISC ? Arrays.stream(TrafficSignShape.values()).filter(x -> {
                return !bookmarks.stream().anyMatch(y -> x == y) || x == TrafficSignShape.MISC;
            }).toArray(TrafficSignShape[]::new) : new TrafficSignShape[] { selectedShape };

            int textureCount = 0;

            for (TrafficSignShape shape : shapes) {
                int a = 1;
                ResourceLocation path = new ResourceLocation(TrafficCraft.MOD_ID + ":" + "textures/block/sign/" + shape.getShape() + "/" + shape.getShape() + a + ".png");
                List<TrafficSignTextureMetadata> locs = new ArrayList<>();
                while (Minecraft.getInstance().getResourceManager().getResource(path).isPresent()) {
                    short width = 32;
                    short height = 32;
                    try (NativeImage img = NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(path).get().open())) {
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
                textureCount += count;
                final TrafficSignShape fShape = shape;
                for (int i = 0; i < count; i++) {
                    final int j = i;
                    DLSprite sprite = new DLSprite(new DLTexture(locs.get(j).location(), 32, 32), ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2, 0, 0, 32, 32);

                    DLToggleButton textureBtn = innerPanel.addComponent(new DLToggleButton(0, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_WIDTH));
                    textureBtn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
                    textureBtn.radioButtonMode.set(true);
                    textureBtn.text.set(TextUtils.EMPTY);
                    textureBtn.icon.set(sprite);
                    textureBtn.iconAlignment.set(ETextAlignment.CENTER);
                    textureBtn.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);
                    textureBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                        CreativePatternCatalogueItem.setCustomImage(stack, NamedTrafficSignTextureReference.ofBuildIn("", new BuildInTrafficSignCodec(locs.get(j).shape(), locs.get(j).id(), locs.get(j).width(), locs.get(j).height())));
                        selectedShape = fShape;
                        return false;
                    });
                }
            }
            
            scrollbar.screenSize.set(ICON_BUTTON_HEIGHT * MAX_ROWS);
            scrollbar.max.set((int)Math.ceil((double)textureCount / MAX_ENTRIES_IN_ROW) * ICON_BUTTON_HEIGHT);
        } else {            
            final int count = PatternCatalogueItem.getStoredPatternCount(stack);
            scrollbar.screenSize.set(ICON_BUTTON_HEIGHT * MAX_ROWS);
            scrollbar.max.set((int)Math.ceil((double)count / MAX_ENTRIES_IN_ROW) * ICON_BUTTON_HEIGHT);
            for (int i = 0; i < count; i++) {
                final int j = i;

                DLToggleButton textureBtn = innerPanel.addComponent(new DLToggleButton(0, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_WIDTH));
                textureBtn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
                textureBtn.radioButtonMode.set(true);
                textureBtn.text.set(TextUtils.EMPTY);
                textureBtn.iconAlignment.set(ETextAlignment.CENTER);
                textureBtn.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);
                textureBtn.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
                    if (e.layer() == RenderLayer.MAIN) {                        
                        NamedTrafficSignTextureReference data = PatternCatalogueItem.getPatternAt(stack, j);
                        TrafficSignClientTexture tex = cachedTextures.computeIfAbsent(data, x -> TrafficSignClientTexture.load(data.getTextureId(), false, null));
                        GuiUtils.drawTexture(tex.getTextureLocation(), e.graphics(), 1, 1, ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2, 0, 0, tex.getRawData().getWidth(), tex.getRawData().getHeight(), TextureFillMode.STRETCH, tex.getRawData().getWidth(), tex.getRawData().getHeight());
                    }
                    return false;
                });
                textureBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                    PatternCatalogueItem.setSelectedIndex(stack, j);
                    if (stack.getItem() instanceof CreativePatternCatalogueItem) {
                        CreativePatternCatalogueItem.clearCustomImage(stack);
                    }
                    return false;
                });
                textureBtn.tooltip.set(new DLTooltip(List.of(TextUtils.text(PatternCatalogueItem.getPatternAt(stack, j).getName())), 200));
            }
        }
    }

    @Override
    public void close() {
        if (selectedShape == null) {
            ModNetworkManager.UPDATE_PATTERN_CATALOG_INDEX.send(NetworkDirection.toServer(), new PatternCatalogueIndexPacket(PatternCatalogueItem.getSelectedIndex(stack)));
        } else {
            NamedTrafficSignTextureReference data = CreativePatternCatalogueItem.getCustomImage(stack);
            if (data != null) {
                ModNetworkManager.UPDATE_CREATIVE_PATTERN_CATALOG_ITEM.send(NetworkDirection.toServer(), new CreativePatternCataloguePacket(data));
            }
        }
        cachedTextures.values().forEach(x -> x.close());
    }


    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.drawTexture(OVERLAY, graphics, width() / 2 - TEXTURE_WIDTH / 2, HEIGHT - TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        if (CreativePatternCatalogueItem.hasCustomPattern(stack)) {            
            NamedTrafficSignTextureReference data = CreativePatternCatalogueItem.getCustomImage(stack);
            TrafficSignClientTexture tex = cachedTextures.computeIfAbsent(data, x -> TrafficSignClientTexture.load(data.getTextureId(), false, null));
            GuiUtils.drawTexture(tex.getTextureLocation(), graphics, WIDTH / 2 - TEXTURE_WIDTH / 2 + 15, HEIGHT - 15 - 24, 24, 24, 0, 0, tex.getRawData().getWidth(), tex.getRawData().getHeight(), TextureFillMode.STRETCH, tex.getRawData().getWidth(), tex.getRawData().getHeight());
        } else {
            NamedTrafficSignTextureReference data = PatternCatalogueItem.getSelectedPattern(stack);
            if (data != null) {
                TrafficSignClientTexture tex = cachedTextures.computeIfAbsent(data, x -> TrafficSignClientTexture.load(data.getTextureId(), false, null));
                GuiUtils.drawTexture(tex.getTextureLocation(), graphics, WIDTH / 2 - TEXTURE_WIDTH / 2 + 15, HEIGHT - 15 - 24, 24, 24, 0, 0, tex.getRawData().getWidth(), tex.getRawData().getHeight(), TextureFillMode.STRETCH, tex.getRawData().getWidth(), tex.getRawData().getHeight());

                float scale = 0.75f;
                graphics.poseStack().pushPose();
                graphics.poseStack().scale(scale, scale, scale);
                GuiUtils.drawString(graphics, graphics.defaultFont(), (int)((WIDTH / 2 - TEXTURE_WIDTH / 2 + 15 + 30) / scale), (int)((HEIGHT - 15 - 24 / 2 - graphics.defaultFont().lineHeight / 2) / scale), data.getName(), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
                graphics.poseStack().popPose();
            }
        }

        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, 0, title, DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.CENTER, true);
    }


    private static class Bookmark extends DLGuiComponent {

        private final DLSprite icon;
        private final boolean right;

        boolean selected;

        public Bookmark(DLSprite icon, boolean right) {
            super(0, 0, BOOKMARK_WIDTH, BOOKMARK_HEIGHT);
            this.icon = icon;
            this.right = right;
        }
        
        @Override
        public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
            GuiUtils.drawTexture(OVERLAY, graphics, 0, 0, width(), height(), BOOKMARK_U, (right ? (selected ? BOOKMARK_V_SELECTED_RIGHT : BOOKMARK_V_UNSELECTED_RIGHT) : (selected ? BOOKMARK_V_SELECTED_LEFT : BOOKMARK_V_UNSELECTED_LEFT)));
            icon.render(graphics, width() / 2 - icon.getWidth() / 2, height() / 2 - icon.getHeight() / 2);
        }

    }
}

package de.mrjulsen.trafficcraft.client.screen;

import java.util.*;

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
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
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
import de.mrjulsen.trafficcraft.block.data.TrafficSignCategory;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.client.ModGuiIcons;
import de.mrjulsen.trafficcraft.data.NamedTextureKey;
import de.mrjulsen.trafficcraft.data.textures.*;
import de.mrjulsen.trafficcraft.data.textures.data.ITextureData;
import de.mrjulsen.trafficcraft.data.textures.data.TrafficSignData;
import de.mrjulsen.trafficcraft.data.textures.decoder.context.IDecoderContext;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.packets.cts.CreativePatternCataloguePacket;
import de.mrjulsen.trafficcraft.network.packets.cts.PatternCatalogueIndexPacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import de.mrjulsen.trafficcraft.util.NaturalOrderComparator;
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

    private static final DLTexture OVERLAY = new DLTexture(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/gui/traffic_sign_workbench_overlay.png"), 256, 256);

    private final DLPanel groupPatterns;
    private final DLPanel innerPanel;
    private DLScrollBar scrollbar;

    private final DLPanel leftBookmarksPanel;
    private final DLPanel rightBookmarksPanel;

    private final Map<TrafficSignCategory, List<ResourceLocation>> signsByCategory;
    private TrafficSignCategory selectedCategory = null;

    private final ItemStack stack;
    private final boolean creative;

    private final LocalTextureCache textureCache = new LocalTextureCache();

    public TrafficSignPatternSelectionScreen(DLWindowManager manager, ItemStack stack) {
        super(manager);
        setSize(WIDTH, HEIGHT);
        windowSpawnPosition.set(WindowPosition.CENTER);

        if (!(stack.getItem() instanceof PatternCatalogueItem)) {
            throw new IllegalStateException("ItemStack is no PatternCatalogueItem.");
        }

        this.stack = stack;
        this.creative = stack.getItem() instanceof CreativePatternCatalogueItem;

        this.signsByCategory = TextureDataManager.INSTANCE.getByTypeGrouped(
                TextureDataTypes.TRAFFIC_SIGN.get(),
                (v) -> true,
                (v) -> TrafficSignCategory.getByName(v.category()).orElse(TrafficSignCategory.MISC));

        groupPatterns = addComponent(new DLPanel(width() / 2 - (ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2) / 2 - 6, HEIGHT - TEXTURE_HEIGHT + 20, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_WIDTH * MAX_ROWS + 2));
        groupPatterns.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);
        groupPatterns.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                DLTextureSheet.DRAGONLIB_UI.getSprite("button_brown_down").render(e.graphics(), 0, 0, s.width(), s.height());
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
        scrollbar.max.set((int) Math.ceil(1.0 / MAX_ENTRIES_IN_ROW * ICON_BUTTON_HEIGHT));
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
            for (TrafficSignCategory category : TrafficSignCategory.values()) {
                final TrafficSignCategory cat = category;
                Bookmark mark = new Bookmark(new DLSprite(new DLTexture(cat.getIconLocation(), 16, 16), 16, 16), false);
                mark.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                    selectedCategory = cat;
                    leftBookmarksPanel.getComponentsOfType(Bookmark.class, true).forEach(a -> a.selected = a == s);
                    rightBookmarksPanel.getComponentsOfType(Bookmark.class, true).forEach(a -> a.selected = a == s);
                    loadTextures();
                    return false;
                });
                leftBookmarksPanel.addComponent(mark);
            }

            Bookmark customMark = new Bookmark(ModGuiIcons.EDIT.getAsSprite(16, 16), true);
            customMark.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                selectedCategory = null;
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
        textureCache.releaseAll();
        innerPanel.clearComponents();

        if (selectedCategory != null) {
            loadBuiltInTextures();
        } else {
            loadCustomTextures();
        }
    }

    private void loadBuiltInTextures() {
        List<TextureIdentifier> keys = new ArrayList<>();

        List<ResourceLocation> textures = signsByCategory.getOrDefault(selectedCategory, List.of());
        textures.sort(Comparator.comparing(ResourceLocation::getPath, new NaturalOrderComparator()));

        for (ResourceLocation location : textures) {
            final TextureIdentifier key = TextureIdentifier.builtIn(TextureDataTypes.TRAFFIC_SIGN.getId(), location);
            final TrafficSignCategory cat = selectedCategory;
            final ITextureData textureData = ClientTextureCache.INSTANCE.getBuiltInTextureData(key).orElse(null);
            if (textureData == null) {
                TrafficCraft.LOGGER.warn("Could not find built-in texture for {}.", key);
                continue;
            }
            keys.add(key);

            DLToggleButton textureBtn = innerPanel.addComponent(new DLToggleButton(0, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT));
            textureBtn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
            textureBtn.radioButtonMode.set(true);
            textureBtn.text.set(TextUtils.EMPTY);
            textureBtn.iconAlignment.set(ETextAlignment.CENTER);
            textureBtn.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);
            ITextureData.ifType(TrafficSignData.class, textureData).ifPresent(signData -> {
                textureBtn.tooltip.set(new DLTooltip(List.of(TextUtils.translate(signData.name())), 200));
            });
            textureBtn.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
                if (e.layer() == RenderLayer.MAIN) {
                    TextureHandle handle = textureCache.getTexture(key, textureData, IDecoderContext.EMPTY);
                    if (handle.isLoaded()) {
                        int w = handle.getTexture().getWidth();
                        int h = handle.getTexture().getHeight();
                        GuiUtils.drawTexture(handle.getLocation(), e.graphics(),
                                1, 1, ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2,
                                0, 0, w, h, TextureFillMode.STRETCH, w, h);
                    }
                }
                return false;
            });
            textureBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                CreativePatternCatalogueItem.setCustomImage(stack, new NamedTextureKey(key, ""));
                this.selectedCategory = cat;
                return false;
            });
        }

        scrollbar.screenSize.set(ICON_BUTTON_HEIGHT * MAX_ROWS);
        scrollbar.max.set((int) Math.ceil((double) keys.size() / MAX_ENTRIES_IN_ROW) * ICON_BUTTON_HEIGHT);
    }

    private void loadCustomTextures() {
        final int count = PatternCatalogueItem.getStoredPatternCount(stack);

        for (int i = 0; i < count; i++) {
            final int j = i;
            NamedTextureKey data = PatternCatalogueItem.getPatternAt(stack, j);

            DLToggleButton textureBtn = innerPanel.addComponent(new DLToggleButton(0, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT));
            textureBtn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
            textureBtn.radioButtonMode.set(true);
            textureBtn.text.set(TextUtils.EMPTY);
            textureBtn.iconAlignment.set(ETextAlignment.CENTER);
            textureBtn.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);
            textureBtn.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
                if (e.layer() == RenderLayer.MAIN) {
                    NamedTextureKey entryData = PatternCatalogueItem.getPatternAt(stack, j);
                    TextureHandle handle = textureCache.getTexture(entryData.textureKey(), IDecoderContext.EMPTY);
                    if (handle.isLoaded()) {
                        int w = handle.getTexture().getWidth();
                        int h = handle.getTexture().getHeight();
                        GuiUtils.drawTexture(handle.getLocation(), e.graphics(),
                                1, 1, ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2,
                                0, 0, w, h, TextureFillMode.STRETCH, w, h);
                    }
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
            textureBtn.tooltip.set(new DLTooltip(List.of(TextUtils.text(data.name())), 200));
        }

        scrollbar.screenSize.set(ICON_BUTTON_HEIGHT * MAX_ROWS);
        scrollbar.max.set((int) Math.ceil((double) count / MAX_ENTRIES_IN_ROW) * ICON_BUTTON_HEIGHT);
    }

    @Override
    public void close() {
        if (selectedCategory == null) {
            ModNetworkManager.UPDATE_PATTERN_CATALOG_INDEX.send(NetworkDirection.toServer(), new PatternCatalogueIndexPacket(PatternCatalogueItem.getSelectedIndex(stack)));
        } else {
            NamedTextureKey data = CreativePatternCatalogueItem.getCustomImage(stack);
            if (data != null) {
                ModNetworkManager.UPDATE_CREATIVE_PATTERN_CATALOG_ITEM.send(NetworkDirection.toServer(), new CreativePatternCataloguePacket(data));
            }
        }
        textureCache.releaseAll();
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.drawTexture(OVERLAY, graphics, width() / 2 - TEXTURE_WIDTH / 2, HEIGHT - TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        if (CreativePatternCatalogueItem.hasCustomPattern(stack)) {
            NamedTextureKey data = CreativePatternCatalogueItem.getCustomImage(stack);
            TextureHandle handle = textureCache.getTexture(data.textureKey(), IDecoderContext.EMPTY);
            if (handle.isLoaded()) {
                int w = handle.getTexture().getWidth();
                int h = handle.getTexture().getHeight();
                GuiUtils.drawTexture(handle.getLocation(), graphics,
                        WIDTH / 2 - TEXTURE_WIDTH / 2 + 15, HEIGHT - 15 - 24, 24, 24,
                        0, 0, w, h,
                        TextureFillMode.STRETCH,
                        w, h);
            }
        } else {
            NamedTextureKey data = PatternCatalogueItem.getSelectedPattern(stack);
            if (data != null) {
                TextureHandle handle = textureCache.getTexture(data.textureKey(), IDecoderContext.EMPTY);
                if (handle.isLoaded()) {
                    int w = handle.getTexture().getWidth();
                    int h = handle.getTexture().getHeight();
                    GuiUtils.drawTexture(handle.getLocation(), graphics,
                            WIDTH / 2 - TEXTURE_WIDTH / 2 + 15, HEIGHT - 15 - 24, 24, 24,
                            0, 0, w, h,
                            TextureFillMode.STRETCH,
                            w, h);

                    float scale = 0.75f;
                    graphics.poseStack().pushPose();
                    graphics.poseStack().scale(scale, scale, scale);
                    GuiUtils.drawString(graphics, graphics.defaultFont(),
                            (int) ((WIDTH / 2 - TEXTURE_WIDTH / 2 + 15 + 30) / scale),
                            (int) ((HEIGHT - 15 - 24 / 2 - graphics.defaultFont().lineHeight / 2) / scale),
                            data.name(), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
                    graphics.poseStack().popPose();
                }
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
            GuiUtils.drawTexture(OVERLAY, graphics, 0, 0, width(), height(), BOOKMARK_U,
                    right
                            ? (selected ? BOOKMARK_V_SELECTED_RIGHT : BOOKMARK_V_UNSELECTED_RIGHT)
                            : (selected ? BOOKMARK_V_SELECTED_LEFT : BOOKMARK_V_UNSELECTED_LEFT));
            icon.render(graphics, width() / 2 - icon.getWidth() / 2, height() / 2 - icon.getHeight() / 2);
        }
    }
}
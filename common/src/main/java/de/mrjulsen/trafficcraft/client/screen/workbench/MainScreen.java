package de.mrjulsen.trafficcraft.client.screen.workbench;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaSimpleButtonRenderer;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils.TextureFillMode;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.client.ModGuiIcons;
import de.mrjulsen.trafficcraft.client.screen.TrafficSignWorkbenchWindow;
import de.mrjulsen.trafficcraft.client.widgets.trafficlight.OptionsPanel;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.data.TrafficSignClientTexture;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.packets.cts.PatternCatalogueDeletePacket;
import de.mrjulsen.trafficcraft.network.packets.cts.PatternCatalogueIndexPacketGui;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class MainScreen extends DLGuiComponent {
    

    private final Component tooltipDefaultNew = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.menu.add");
    private final Component tooltipDefaultEdit = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.menu.edit");
    private final Component tooltipDefaultDelete = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.menu.delete");
    private final Component emptyPattern = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.menu.no_pattern");
    
    private final TrafficSignWorkbenchWindow win;
    private NamedTrafficSignTextureReference preview;
    
    private final Map<NamedTrafficSignTextureReference, TrafficSignClientTexture> cachedTextures = new HashMap<>();

    public MainScreen(TrafficSignWorkbenchWindow win) {
        super(0, 0, win.width(), win.height());
        this.win = win;
        layoutContraint.set(FlowLayout.FlowConstraint.FILL);
        
        //#region DEFAULT MODE CONTROLS

        OptionsPanel btnPanel = new OptionsPanel(FlowLayout.Direction.VERTICAL);
        btnPanel.setPosition(8, 36);
        addComponent(btnPanel);

        DLButton btnNew = new DLButton(0, 0, 18, 18);
        btnNew.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
        btnNew.text.set(TextUtils.empty());
        btnNew.icon.set(ModGuiIcons.ADD.getAsSprite(16, 16));
        btnNew.tooltip.set(new DLTooltip(List.of(tooltipDefaultNew), 200));
        btnNew.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            win.clearComponents();
            win.addComponent(new PatternSelectionScreen(win));
            return false;
        });

        DLButton btnEdit = new DLButton(0, 0, 18, 18);
        btnEdit.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
        btnEdit.text.set(TextUtils.empty());
        btnEdit.icon.set(ModGuiIcons.EDIT.getAsSprite(16, 16));
        btnEdit.tooltip.set(new DLTooltip(List.of(tooltipDefaultEdit), 200));
        btnEdit.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getPrevievTexture().getTexture();
            if (preview == null) {
                return false;
            }
            win.clearComponents();
            win.addComponent(new EditorScreen(win, getPrevievTexture().getRawData().getShape(), getPrevievTexture().getTexture(), preview.getName(), PatternCatalogueItem.getSelectedIndex(win.getMenu().patternSlot.getItem())));
            return false;
        });

        DLButton btnDelete = new DLButton(0, 0, 18, 18);
        btnDelete.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
        btnDelete.text.set(TextUtils.empty());
        btnDelete.icon.set(ModGuiIcons.DELETE.getAsSprite(16, 16));
        btnDelete.tooltip.set(new DLTooltip(List.of(tooltipDefaultDelete), 200));
        btnDelete.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            if (preview == null) {
                return false;
            }
            
            final Screen currentScreen = Minecraft.getInstance().screen;
            Minecraft.getInstance().setScreen(new ConfirmScreen((b) -> {
                if (b) {
                    int idx = PatternCatalogueItem.getSelectedIndex(win.getMenu().patternSlot.getItem());
                    ModNetworkManager.DELETE_PATTERN_CATALOG_ENTRY.send(NetworkDirection.toServer(), new PatternCatalogueDeletePacket.Request(idx), (response) -> {
                        updatePreview();
                    }, () -> {});
                }
                Minecraft.getInstance().setScreen(currentScreen);
            },
            TextUtils.translate("gui.trafficcraft.trafficsignworkbench.delete.question"),
            TextUtils.translate("selectWorld.deleteWarning", preview.getName()),
            TextUtils.translate("selectWorld.deleteButton"),
            CommonComponents.GUI_CANCEL));
            return false;
        });

        btnPanel.addComponent(btnNew);
        btnPanel.addComponent(btnEdit);
        btnPanel.addComponent(btnDelete);


        PageButton prevBtn = addComponent(new PageButton(51, 164, -1));
        PageButton nextBtn = addComponent(new PageButton(149, 164, 1));

        prevBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            switchPreview(-1);
            return false;
        });
        
        nextBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            switchPreview(1);
            return false;
        });

        updatePreview();
    }

    private void switchPreview(int index) {
        int dx = MathUtils.clamp(PatternCatalogueItem.getSelectedIndex(win.getMenu().patternSlot.getItem()) + index, 0, PatternCatalogueItem.getStoredPatternCount(win.getMenu().patternSlot.getItem()) - 1);
        PatternCatalogueItem.setSelectedIndex(win.getMenu().patternSlot.getItem(), dx);
        ModNetworkManager.UPDATE_PATTERN_CATALOG_INDEX_IN_GUI.send(NetworkDirection.toServer(), new PatternCatalogueIndexPacketGui.Request(dx), (response) -> {
            updatePreview();
        }, () -> {});
    }

    private void initPreview() {
        this.preview = PatternCatalogueItem.getSelectedPattern(win.getMenu().patternSlot.getItem());
        getPrevievTexture();
    }

    private synchronized TrafficSignClientTexture getPrevievTexture() {
        if (preview == null) {
            return TrafficSignClientTexture.EMPTY;
        }
        return cachedTextures.computeIfAbsent(preview, x -> TrafficSignClientTexture.load(x.getTextureId(), false, () -> {
            updatePreview();
        }));
    }

    public void updatePreview() {
        this.initPreview();
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        String label = "";
        if (preview != null) { 
            TrafficSignClientTexture tex = getPrevievTexture(); 
            GuiUtils.drawTexture(tex.getTextureLocation(), graphics, width() / 2 - 50, 40, 100, 100, 0, 0, tex.getRawData().getWidth(), tex.getRawData().getHeight(), TextureFillMode.STRETCH, tex.getRawData().getWidth(), tex.getRawData().getHeight());
        } else {
            label = emptyPattern.getString();
            GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, 80, label, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.CENTER, false);
        }
        
        label = String.format("%s / %s", PatternCatalogueItem.getSelectedIndex(win.getMenu().patternSlot.getItem()) + 1, PatternCatalogueItem.getStoredPatternCount(win.getMenu().patternSlot.getItem()));
        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, 170 - graphics.defaultFont().lineHeight / 2, label, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.CENTER, false);
        label = preview == null ? "" : preview.getName();
        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, 155 - graphics.defaultFont().lineHeight / 2, label, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.CENTER, false);
    }


    private static class PageButton extends DLGuiComponent {

        private static final DLTexture TEXTURE = new DLTexture(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/gui/traffic_sign_workbench_overlay.png"), 256, 256);
        private final int direction;

        public PageButton(int x, int y, int direction) {
            super(x, y, 23, 13);
            this.direction = direction;
        }

        @Override
        public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
            GuiUtils.drawTexture(TEXTURE, graphics, 0, 0, width(), height(), isSelected() ? 23 : 0, 174 + (direction < 0 ? 13 : 0));
        }
        
    }
    
}

package de.mrjulsen.trafficcraft.client.screen.workbench;

import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;

import de.mrjulsen.mcdragonlib.client.gui.builtin.DLColorPickerWindow;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaSimpleButtonRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.DLColor.ColorChannel;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.client.ModGuiIcons;
import de.mrjulsen.trafficcraft.client.screen.SignPickerScreen;
import de.mrjulsen.trafficcraft.client.screen.TrafficSignWorkbenchWindow;
import de.mrjulsen.trafficcraft.client.screen.workbench.Canvas.EditorConfig;
import de.mrjulsen.trafficcraft.client.widgets.trafficlight.OptionButton;
import de.mrjulsen.trafficcraft.client.widgets.trafficlight.OptionsPanel;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.data.TrafficSignClientTexture;
import de.mrjulsen.trafficcraft.data.TrafficSignTextureData;
import de.mrjulsen.trafficcraft.init.ClientInit;
import de.mrjulsen.trafficcraft.item.ColorPaletteItem;
import de.mrjulsen.trafficcraft.network.packets.cts.ColorPaletteItemPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficSignPatternPacket;
import de.mrjulsen.trafficcraft.registry.ModItems;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

public class EditorScreen extends DLGuiComponent {

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
    

    private final Component tooltipEditorToolbarDraw = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.editor.draw");
    private final Component tooltipEditorToolbarErase = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.editor.erase");
    private final Component tooltipEditorToolbarPickColor = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.editor.pick_color");
    private final Component tooltipEditorToolbarFill = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.editor.fill");
    private final Component tooltipEditorToolbarText = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.editor.text");
    private final Component tooltipEditorToolbarLoad = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.editor.load");
    private final Component tooltipEditorToolbarSave = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.editor.save");
    private final Component tooltipEditorToolbarDiscard = TextUtils.translate("gui.trafficcraft.trafficsignworkbench.editor.discard");

    private final TrafficSignWorkbenchWindow win;
    private final TrafficSignShape shape;
    private final EditorConfig config = new EditorConfig();

    private final int editIndex;

    private OptionsPanel colorsPanel;
    private boolean hasColorPalette;


    public EditorScreen(TrafficSignWorkbenchWindow win, TrafficSignShape shape, DynamicTexture texture, String name, int index) {
        super(0, 0, win.width(), win.height());
        this.win = win;
        layoutContraint.set(FlowLayout.FlowConstraint.FILL);

        this.shape = shape;

        Canvas canvas = addComponent(new Canvas(width() / 2 - 66, 32, shape, config));

        DLRichTextEditBox textBox = addComponent(new DLRichTextEditBox(width() / 2 - 63, 164, 120, 10));
        textBox.maxCharacters.set(20);
        textBox.componentRenderer.set(SlotTextboxRenderer.INSTANCE);

        if (texture != null && name != null && index >= 0) {
            canvas.pixels = ClientInit.textureToIntArray(texture, true);
            textBox.text.get().set(name);
            this.editIndex = index;
        } else {
            this.editIndex = -1;
            textBox.text.get().set(TextUtils.translate("gui.trafficcraft.trafficsignworkbench.pattern.name_unknown").getString());
        }
        

        OptionsPanel btnPanel = new OptionsPanel(FlowLayout.Direction.VERTICAL);
        btnPanel.setPosition(8, 36);
        addComponent(btnPanel);

        OptionButton btnEdit = new OptionButton(ModGuiIcons.EDIT.getAsSprite(16, 16));
        btnEdit.tooltip.set(new DLTooltip(List.of(tooltipEditorToolbarDraw), 200));
        btnEdit.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            config.tool = TrafficSignWorkbenchEditorTool.DRAW;
            return false;
        });

        OptionButton btnErase = new OptionButton(ModGuiIcons.ERASE.getAsSprite(16, 16));
        btnErase.tooltip.set(new DLTooltip(List.of(tooltipEditorToolbarErase), 200));
        btnErase.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            config.tool = TrafficSignWorkbenchEditorTool.ERASER;
            return false;
        });

        OptionButton btnPick = new OptionButton(ModGuiIcons.PICK.getAsSprite(16, 16));
        btnPick.tooltip.set(new DLTooltip(List.of(tooltipEditorToolbarPickColor), 200));
        btnPick.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            config.tool = TrafficSignWorkbenchEditorTool.PICK_COLOR;
            return false;
        });

        OptionButton btnFill = new OptionButton(ModGuiIcons.FILL.getAsSprite(16, 16));
        btnFill.tooltip.set(new DLTooltip(List.of(tooltipEditorToolbarFill), 200));
        btnFill.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            config.tool = TrafficSignWorkbenchEditorTool.FILL;
            return false;
        });




        OptionsPanel panelSaveOptions = new OptionsPanel(FlowLayout.Direction.VERTICAL);
        panelSaveOptions.setPosition(8, win.height() - 18 * 4);
        addComponent(panelSaveOptions);

        DLButton btnLoad = new DLButton(0, 0, 18, 18);
        btnLoad.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
        btnLoad.text.set(TextUtils.empty());
        btnLoad.icon.set(ModGuiIcons.OPEN.getAsSprite(16, 16));
        btnLoad.tooltip.set(new DLTooltip(List.of(tooltipEditorToolbarLoad), 200));
        btnLoad.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().createModal(mgr -> new SignPickerScreen(mgr, shape, (image) -> {
                if (image != null) {
                    for (int a = 0; a < TrafficSignShape.MAX_WIDTH; a++) {
                        for (int b = 0; b < TrafficSignShape.MAX_HEIGHT; b++) {
                            canvas.pixels[a][b] = DLColor.fromInt(image.getPixelRGBA(a, b)).swapChannels(ColorChannel.R, ColorChannel.B).getAsARGB(); 
                        }
                    }
                }
            }));
            return false;
        });

        DLButton btnSave = new DLButton(0, 0, 18, 18);
        btnSave.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
        btnSave.text.set(TextUtils.empty());
        btnSave.icon.set(ModGuiIcons.SAVE.getAsSprite(16, 16));
        btnSave.tooltip.set(new DLTooltip(List.of(tooltipEditorToolbarSave), 200));
        btnSave.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            NativeImage img = new NativeImage(Format.RGBA, TrafficSignShape.MAX_WIDTH, TrafficSignShape.MAX_HEIGHT, false);
            for (int k = 0; k < img.getWidth(); k++) {
                for (int l = 0; l < img.getHeight(); l++) {
                    img.setPixelRGBA(k, l, 0);
                    if (shape.isPixelValid(k, l))
                        img.setPixelRGBA(k, l, DLColor.fromInt(canvas.pixels[k][l]).swapChannels(ColorChannel.R, ColorChannel.B).getAsARGB());
                }
            }
            
            TrafficSignTextureData data = TrafficSignClientTexture.createNew(shape, img, null);
            NamedTrafficSignTextureReference ref = NamedTrafficSignTextureReference.of(data, textBox.text.get().getPlainText());
            ModNetworkManager.UPDATE_TRAFFIC_SIGN_PATTERN.send(NetworkDirection.toServer(), new TrafficSignPatternPacket.Request(ref, editIndex), (response) -> {
                closePage();
            }, () -> {});
            img.close();
            return false;
        });

        DLButton btnDiscard = new DLButton(0, 0, 18, 18);
        btnDiscard.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
        btnDiscard.text.set(TextUtils.empty());
        btnDiscard.icon.set(ModGuiIcons.DISCARD_FILE.getAsSprite(16, 16));
        btnDiscard.tooltip.set(new DLTooltip(List.of(tooltipEditorToolbarDiscard), 200));
        btnDiscard.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            closePage();
            return false;
        });



        OptionsPanel pickColorPanel = new OptionsPanel(FlowLayout.Direction.VERTICAL);
        pickColorPanel.setPosition(win.width() - 28, 36);
        addComponent(pickColorPanel);

        DLButton btnPickColor = new DLButton(0, 0, 18, 18);
        btnPickColor.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
        btnPickColor.text.set(TextUtils.empty());        
        btnPickColor.tooltip.set(new DLTooltip(List.of(tooltipEditorToolbarPickColor), 200));
        btnPickColor.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                GuiUtils.fill(e.graphics(), 2, 2, s.width() - 4, s.height() - 4, config.color);
                ModGuiIcons.ADD_BULLET.render(e.graphics(), 1, 1);
            }
            return false;
        });
        btnPickColor.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().createModal(mgr -> new DLColorPickerWindow(mgr, false, config.color, color -> config.color = color));
            return false;
        });



        colorsPanel = new OptionsPanel(FlowLayout.Direction.VERTICAL);
        colorsPanel.setPosition(pickColorPanel.x(), pickColorPanel.y() + 25);
        addComponent(colorsPanel);


        btnPanel.addComponent(btnEdit);
        btnPanel.addComponent(btnErase);
        btnPanel.addComponent(btnPick);
        btnPanel.addComponent(btnFill);

        panelSaveOptions.addComponent(btnLoad);
        panelSaveOptions.addComponent(btnSave);
        panelSaveOptions.addComponent(btnDiscard);

        pickColorPanel.addComponent(btnPickColor);

        config.tool = TrafficSignWorkbenchEditorTool.DRAW;
        config.color = DLColor.BLACK;
    }

    private void closePage() {        
        win.clearComponents();
        win.addComponent(new MainScreen(win));
    }

    @Override
    public void tick() {
        ItemStack stack = win.getMenu().colorSlot.getItem();
        boolean hasItem = !stack.isEmpty();
        boolean changed = hasColorPalette != hasItem;

        if (changed) {
            hasColorPalette = hasItem;
            if (hasColorPalette) {
                reloadColors();
            } else {
                colorsPanel.clearComponents();
            }
        }
    }

    private void reloadColors() {
        ItemStack stack = win.getMenu().colorSlot.getItem();
        colorsPanel.clearComponents();
        if (stack.is(ModItems.COLOR_PALETTE.get())) {
            for (int i = 0; i < ColorPaletteItem.MAX_COLORS; i++) { 
                final int k = i;
                final Supplier<DLColor> colorGetter = () -> DLColor.fromInt(ColorPaletteItem.getColorAt(stack, k));
                DLButton btn = new DLButton(0, 0, 18, 18);
                btn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
                btn.text.set(TextUtils.empty());
                btn.tooltip.set(new DLTooltip(List.of(tooltipEditorToolbarDiscard), 200));
                btn.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
                    if (e.layer() == RenderLayer.MAIN) {
                        GuiUtils.fill(e.graphics(), 2, 2, s.width() - 4, s.height() - 4, colorGetter.get());
                    }
                    return false;
                });
                btn.tooltip.set(new DLTooltip(List.of(
                    TextUtils.translate("gui.trafficcraft.trafficsignworkbench.editor.color.slot", k + 1).withStyle(Style.EMPTY.withColor(TextColor.fromRgb((colorGetter.get().isTransparent() ? DLColor.WHITE : colorGetter.get()).getAsARGB()))),
                    TextUtils.translate("gui.trafficcraft.trafficsignworkbench.editor.color.get").withStyle(ChatFormatting.GRAY),
                    TextUtils.translate("gui.trafficcraft.trafficsignworkbench.editor.color.set").withStyle(ChatFormatting.GRAY),
                    TextUtils.translate("gui.trafficcraft.trafficsignworkbench.editor.color.remove").withStyle(ChatFormatting.GRAY)
                ), 200));
                btn.addEventListener(DLGuiStandardEvents.MousePressedEvent.class, (s, e) -> {
                    switch (e.button()) {
                        case GLFW.GLFW_MOUSE_BUTTON_LEFT:
                            DLColor col = colorGetter.get();
                            if (!col.isTransparent()) {
                                config.color = col;
                            }
                            break;
                        case GLFW.GLFW_MOUSE_BUTTON_RIGHT:
                            ColorPaletteItem.setColor(stack, k, config.color.getAsARGB());
                            win.getMenu().colorSlot.set(stack);
                            win.getMenu().colorSlot.setChanged();
                            win.getMenu().broadcastChanges();
                            ModNetworkManager.UPDATE_COLOR_PALETTE_ITEM.send(NetworkDirection.toServer(), new ColorPaletteItemPacket.Request(config.color.getAsARGB(), k), (response) -> {
                                reloadColors();                                
                            }, () -> {});
                            break;
                        case GLFW.GLFW_MOUSE_BUTTON_MIDDLE:
                            ColorPaletteItem.setColor(stack, k, 0);
                            win.getMenu().colorSlot.set(stack);
                            win.getMenu().colorSlot.setChanged();
                            win.getMenu().broadcastChanges();
                            ModNetworkManager.UPDATE_COLOR_PALETTE_ITEM.send(NetworkDirection.toServer(), new ColorPaletteItemPacket.Request(0, k), (response) -> {
                                reloadColors();
                            }, () -> {});
                            break;
                        default:
                            break;
                    }
                    return false;
                });
                colorsPanel.addComponent(btn);
            }
        }
    }
    
}

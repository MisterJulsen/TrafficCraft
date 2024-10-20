package de.mrjulsen.trafficcraft.client.screen;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
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
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SignPickerScreen extends DLScreen {

    public static final Component title = TextUtils.translate("gui.trafficcraft.signpicker.title");
    public static final Component titleOpenFileDialog = TextUtils.translate("gui.trafficcraft.signpicker.openfiledialog");
    public static final Component btnDoneText = TextUtils.translate("gui.trafficcraft.signpicker.load");
    public static final Component tooltipImport = TextUtils.translate("gui.trafficcraft.signpicker.tooltip.import");

    private static final int WIDTH = 187;
    private static final int HEIGHT = 171;
    private static final int MAX_ENTRIES_IN_ROW = 9;
    private static final int MAX_ROWS = 6;
    private static final int ICON_BUTTON_WIDTH = 18;
    private static final int ICON_BUTTON_HEIGHT = 18;
      
    private int guiLeft;
    private int guiTop;
    private DynamicTexture preview;

    private double scroll = 0;

    private final Screen lastScreen;
    private final TrafficSignShape shape;

    private final WidgetsCollection groupPatterns = new WidgetsCollection();
    private DLVerticalScrollBar scrollbar;
    private DLButton doneButton;

    private final ResourceLocation[] resources;
    private final int count;
    private final Consumer<NativeImage> result;

    public SignPickerScreen(Screen lastScreen, TrafficSignShape shape, Consumer<NativeImage> result) {
        super(title);
        this.lastScreen = lastScreen;
        this.shape = shape;
        this.result = result;

        int i = 1;
        ResourceLocation path = new ResourceLocation(TrafficCraft.MOD_ID + ":" + "textures/block/sign/" + shape.getShape() + "/" + shape.getShape() + i + ".png");
        List<ResourceLocation> locs = new ArrayList<>();
        while (Minecraft.getInstance().getResourceManager().hasResource(path)) {
            locs.add(path);
            i++;
            path = new ResourceLocation(TrafficCraft.MOD_ID + ":" + "textures/block/sign/" + shape.getShape() + "/" + shape.getShape() + i + ".png");
        }
        this.resources = locs.toArray(ResourceLocation[]::new);
        this.count = this.resources.length;
    }

    @Override
    public void onClose() {
        if (lastScreen != null) {            
            this.minecraft.setScreen(this.lastScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    public void tick() {
        doneButton.set_active(preview != null);
        super.tick();
    }

    @Override
    public void init() {
        super.init();
        guiLeft = this.width / 2 - WIDTH / 2;
        guiTop = this.height / 2 - (HEIGHT + 24) / 2; 

        groupPatterns.components.clear();
        
        doneButton = addButton(guiLeft + WIDTH / 2 - 67 + 20, guiTop + HEIGHT - 28, 65, 20, btnDoneText, (p) -> {
            this.onDone();
        }, null);

        addButton(guiLeft + WIDTH / 2 + 2 + 20, guiTop + HEIGHT - 28, 65, 20, CommonComponents.GUI_CANCEL, (p) -> {
            this.onClose();
        }, null);

        
        DLIconButton btn = new DLIconButton(ButtonType.DEFAULT, AreaStyle.BROWN, TrafficSignWorkbenchGui.ButtonIcons.IMPORT.getSprite(), groupPatterns, guiLeft + 9, guiTop + 36 + 0 * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, TextUtils.empty(), (button) -> {
            groupPatterns.performForEach(x -> ((DLIconButton)x).deselect());
            PointerBuffer filterPatterns = MemoryUtil.memAllocPointer(5);
            filterPatterns.put(MemoryUtil.memUTF8("*.png"));
            filterPatterns.put(MemoryUtil.memUTF8("*.jpg"));
            filterPatterns.put(MemoryUtil.memUTF8("*.jpeg"));
            filterPatterns.put(MemoryUtil.memUTF8("*.gif"));
            filterPatterns.put(MemoryUtil.memUTF8("*.bmp"));
            filterPatterns.flip();

            this.minecraft.getSoundManager().pause();
            String s = TinyFileDialogs.tinyfd_openFileDialog(titleOpenFileDialog.getString(), (CharSequence)null, filterPatterns, "Image Files", false);
            if (s != null) {
                try (InputStream data = DLUtils.scaleImage(new FileInputStream(s), 32, 32)) {
                    if (preview != null) {
                        preview.close();
                        preview = null;                    
                    }
                    NativeImage img = NativeImage.read(data);
                    Arrays.stream(shape.getInvalidPixels()).forEach(c -> {
                        byte[] coords = DLUtils.intToCoords(c);
                        byte x = coords[0];
                        byte y = coords[1];
                        img.setPixelRGBA(x, y, 0);
                    });
                    preview = new DynamicTexture(img);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
            this.minecraft.getSoundManager().resume();
        }).withAlignment(EAlignment.CENTER);
        addTooltip(DLTooltip.of(tooltipImport).assignedTo(btn)).withMaxWidth(width / 4);
        this.addRenderableWidget(btn);
        
        for (int i = 0; i < count; i++) {
            final int j = i;
            Sprite sprite = new Sprite(resources[j], 32, 32, 0, 0, 32, 32, ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2);
            DLIconButton btnImport = new DLIconButton(ButtonType.RADIO_BUTTON, AreaStyle.BROWN, sprite, groupPatterns, guiLeft + 9, guiTop + 36 + j * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, TextUtils.empty(), (button) -> {
                if (preview != null) {
                    preview.close();
                    preview = null;
                }

                try {
                    preview = new DynamicTexture(NativeImage.read(this.minecraft.getResourceManager().getResource(resources[j]).getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).withAlignment(EAlignment.CENTER);            
            this.addRenderableWidget(btnImport);
        }
        

        this.scrollbar = this.addRenderableWidget(new DLVerticalScrollBar(guiLeft + 171, guiTop + 16, 8, ICON_BUTTON_HEIGHT * MAX_ROWS + 2, new GuiAreaDefinition(guiLeft + 7, guiTop + 16, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_HEIGHT * MAX_ROWS + 2)).withOnValueChanged(v -> {
            this.scroll = v.getScrollValue();
            fillButtons(groupPatterns.components.toArray(DLIconButton[]::new), this.scroll, guiLeft + 8, guiTop + 17, scrollbar);
        }).setAutoScrollerSize(true));

        fillButtons(groupPatterns.components.toArray(DLIconButton[]::new), this.scroll, guiLeft + 8, guiTop + 17, scrollbar);
    }

    @Override
    protected void onDone() {
        NativeImage img = null;
        if (preview != null) {
            final NativeImage image = preview.getPixels();
            Arrays.stream(shape.getInvalidPixels()).forEach(c -> {
                byte[] coords = DLUtils.intToCoords(c);
                byte x = coords[0];
                byte y = coords[1];
                image.setPixelRGBA(x, y, 0);
            });
            img = image;
        }
        result.accept(img);
        this.onClose();
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderScreenBackground(graphics);
        DynamicGuiRenderer.renderWindow(graphics, guiLeft, guiTop, WIDTH, HEIGHT);
        DynamicGuiRenderer.renderArea(graphics, guiLeft + 7, guiTop + 16, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_HEIGHT * MAX_ROWS + 2, AreaStyle.BROWN, ButtonState.DOWN);
        
        GuiUtils.drawString(graphics, font, guiLeft + WIDTH / 2, guiTop + 6, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.CENTER, false);

        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
        
        if (preview != null) {
            GuiUtils.drawTexture(preview.getId(), graphics, guiLeft + 8, guiTop + 130, 32, 32, 0, 0, 32, 32, 32, 32);
        }
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
            scrollbar.setScreenSize(MAX_ROWS).updateMaxScroll(currentRow + 1);
        }
    }
}

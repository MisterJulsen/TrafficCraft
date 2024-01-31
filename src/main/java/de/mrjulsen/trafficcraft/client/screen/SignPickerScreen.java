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

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
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
import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SignPickerScreen extends CommonScreen {

    public static final Component title = Utils.translate("gui.trafficcraft.signpicker.title");
    public static final Component titleOpenFileDialog = Utils.translate("gui.trafficcraft.signpicker.openfiledialog");
    public static final Component btnDoneText = Utils.translate("gui.trafficcraft.signpicker.load");
    public static final Component tooltipImport = Utils.translate("gui.trafficcraft.signpicker.tooltip.import");

    private static final int WIDTH = 187;
    private static final int HEIGHT = 171;
    private static final int MAX_ENTRIES_IN_ROW = 9;
    private static final int MAX_ROWS = 6;
    private static final int ICON_BUTTON_WIDTH = 18;
    private static final int ICON_BUTTON_HEIGHT = 18;
      
    private int guiLeft;
    private int guiTop;
    private DynamicTexture preview;

    private int scroll = 0;

    private final Screen lastScreen;
    private final TrafficSignShape shape;

    private final WidgetsCollection groupPatterns = new WidgetsCollection();
    private VerticalScrollBar scrollbar;
    private Button doneButton;

    private final ResourceLocation[] resources;
    private final int count;
    private final Consumer<NativeImage> result;

    public SignPickerScreen(Screen lastScreen, TrafficSignShape shape, Consumer<NativeImage> result) {
        super(title);
        this.lastScreen = lastScreen;
        this.shape = shape;
        this.result = result;

        int i = 1;
        ResourceLocation path = new ResourceLocation(ModMain.MOD_ID + ":" + "textures/block/sign/" + shape.getShape() + "/" + shape.getShape() + i + ".png");
        List<ResourceLocation> locs = new ArrayList<>();
        while (Minecraft.getInstance().getResourceManager().getResource(path).isPresent()) {
            locs.add(path);
            i++;
            path = new ResourceLocation(ModMain.MOD_ID + ":" + "textures/block/sign/" + shape.getShape() + "/" + shape.getShape() + i + ".png");
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
        doneButton.active = preview != null;
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

        
        IconButton btn = new IconButton(ButtonType.DEFAULT, AreaStyle.BROWN, TrafficSignWorkbenchGui.ButtonIcons.IMPORT.getSprite(), groupPatterns, guiLeft + 9, guiTop + 36 + 0 * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, null, (button) -> {
            groupPatterns.performForEach(x -> ((IconButton)x).deselect());
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
                try (InputStream data = Utils.scaleImage(new FileInputStream(s), 32, 32)) {
                    if (preview != null) {
                        preview.close();
                        preview = null;                    
                    }
                    NativeImage img = NativeImage.read(data);
                    Arrays.stream(shape.getInvalidPixels()).forEach(c -> {
                        byte[] coords = Utils.intToCoords(c);
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
        }).withAlignment(Alignment.CENTER);
        addTooltip(Tooltip.of(tooltipImport).assignedTo(btn)).withMaxWidth(width / 4);
        this.addRenderableWidget(btn);
        
        for (int i = 0; i < count; i++) {
            final int j = i;
            Sprite sprite = new Sprite(resources[j], 32, 32, 0, 0, 32, 32, ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2);
            IconButton btnImport = new IconButton(ButtonType.RADIO_BUTTON, AreaStyle.BROWN, sprite, groupPatterns, guiLeft + 9, guiTop + 36 + j * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, null, (button) -> {
                if (preview != null) {
                    preview.close();
                    preview = null;
                }

                try {
                    preview = new DynamicTexture(NativeImage.read(this.minecraft.getResourceManager().getResource(resources[j]).get().open()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).withAlignment(Alignment.CENTER);            
            this.addRenderableWidget(btnImport);
        }
        

        this.scrollbar = this.addRenderableWidget(new VerticalScrollBar(guiLeft + 171, guiTop + 16, 8, ICON_BUTTON_HEIGHT * MAX_ROWS + 2, new GuiAreaDefinition(guiLeft + 7, guiTop + 16, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_HEIGHT * MAX_ROWS + 2)).setOnValueChangedEvent(v -> {
            this.scroll = v.getScrollValue();
            fillButtons(groupPatterns.components.toArray(IconButton[]::new), this.scroll, guiLeft + 8, guiTop + 17, scrollbar);
        }).setAutoScrollerHeight(true));

        fillButtons(groupPatterns.components.toArray(IconButton[]::new), this.scroll, guiLeft + 8, guiTop + 17, scrollbar);
    }

    @Override
    protected void onDone() {
        NativeImage img = null;
        if (preview != null) {
            final NativeImage image = preview.getPixels();
            Arrays.stream(shape.getInvalidPixels()).forEach(c -> {
                byte[] coords = Utils.intToCoords(c);
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
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack);
        DynamicGuiRenderer.renderWindow(stack, guiLeft, guiTop, WIDTH, HEIGHT);
        DynamicGuiRenderer.renderArea(stack, guiLeft + 7, guiTop + 16, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_HEIGHT * MAX_ROWS + 2, AreaStyle.BROWN, ButtonState.SUNKEN);
        
        this.font.draw(stack, title, guiLeft + WIDTH / 2 - font.width(title) / 2, guiTop + 6, 4210752);

        super.render(stack, mouseX, mouseY, partialTicks);
        
        if (preview != null) {
            GuiUtils.blit(preview.getId(), stack, guiLeft + 8, guiTop + 130, 32, 32, 0, 0, 32, 32, 32, 32);
        }
    }

    private void fillButtons(IconButton[] buttons, int scrollRow, int defX, int defY, VerticalScrollBar scrollbar) {
        if (buttons.length <= 0) {
            return;
        }

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

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.shouldCloseOnEsc() && pKeyCode == 256 || this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }
}

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
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLToggleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
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
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.client.ModGuiIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SignPickerScreen extends DLWindow {

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
      
    private DynamicTexture preview;

    private final TrafficSignShape shape;

    private final DLPanel groupPatterns;
    private DLScrollBar scrollbar;
    private DLButton doneButton;

    private final DLTexture[] resources;
    private final int count;
    private final Consumer<NativeImage> result;

    public SignPickerScreen(DLWindowManager manager, TrafficSignShape shape, Consumer<NativeImage> result) {
        super(manager);
        this.shape = shape;
        this.result = result;

        setSize(WIDTH, HEIGHT);
        windowSpawnPosition.set(WindowPosition.PARENT_CENTER);

        int i = 1;
        ResourceLocation path = DLUtils.resourceLocation(TrafficCraft.MOD_ID + ":" + "textures/block/sign/" + shape.getShape() + "/" + shape.getShape() + i + ".png");
        List<DLTexture> locs = new ArrayList<>();
        while (Minecraft.getInstance().getResourceManager().getResource(path).isPresent()) {
            locs.add(new DLTexture(path, 32, 32));
            i++;
            path = new ResourceLocation(TrafficCraft.MOD_ID + ":" + "textures/block/sign/" + shape.getShape() + "/" + shape.getShape() + i + ".png");
        }
        this.resources = locs.toArray(DLTexture[]::new);
        this.count = this.resources.length;

        groupPatterns = addComponent(new DLPanel(7, 16, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_WIDTH * MAX_ROWS + 2));
        groupPatterns.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);
        groupPatterns.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                DefaultGuiTextures.DRAGONLIB_UI.getSprite("button_brown_down").render(e.graphics(), 0, 0, s.width(), s.height());
            }
            return false;
        });

        DLPanel innerPanel = groupPatterns.addComponent(new DLPanel(1, 1, groupPatterns.width() - 2, groupPatterns.height() - 2));
        FlowLayout layout = new FlowLayout();
        layout.flowDirection.set(Direction.HORIZONTAL);
        layout.wrap.set(true);        
        innerPanel.layout.set(layout);
        innerPanel.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);

        this.scrollbar = addComponent(new DLScrollBar(groupPatterns.x() + groupPatterns.width(), groupPatterns.y(), 8, groupPatterns.height(), Orientation.VERTICAL));
        scrollbar.anchor.set2(EAlign.BOTTOM, EAlign.TOP, EAlign.RIGHT);
        scrollbar.scrollerSize.set(0);
        scrollbar.screenSize.set(innerPanel.height());
        scrollbar.max.set((int)Math.ceil(count / MAX_ENTRIES_IN_ROW * ICON_BUTTON_HEIGHT));
        scrollbar.inputConsumptionPolicy.set(c -> true);
        scrollbar.scrollSteps.set(ICON_BUTTON_HEIGHT);
        scrollbar.addEventListener(DLScrollBar.ValueChangedEvent.class, (s, e) -> {
            innerPanel.setScrollOffsetY(e.value());
            return false;
        });
        addEventListener(DLGuiStandardEvents.ScrollEvent.class, scrollbar::invokeEvent);
        
        doneButton = addComponent(new DLButton(WIDTH / 2 - 67 + 20, HEIGHT - 28, 65, 20));
        doneButton.text.set(btnDoneText);
        doneButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            onDone();
            return false;
        });

        DLButton cancelButton = addComponent(new DLButton(WIDTH / 2 + 2 + 20, HEIGHT - 28, 65, 20));
        cancelButton.text.set(CommonComponents.GUI_CANCEL);
        cancelButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().closeWindow(this);
            return false;
        });


        
        DLButton btnImport = new DLButton(0, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
        btnImport.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
        btnImport.tooltip.set(new DLTooltip(List.of(tooltipImport), 200));
        btnImport.text.set(TextUtils.empty());
        btnImport.icon.set(ModGuiIcons.WRITE_TO_FILE.getAsSprite(16, 16));
        innerPanel.addComponent(btnImport);        
        btnImport.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            groupPatterns.getComponentsOfType(DLToggleButton.class, true).forEach(x -> x.checked.set(false));
            PointerBuffer filterPatterns = MemoryUtil.memAllocPointer(5);
            filterPatterns.put(MemoryUtil.memUTF8("*.png"));
            filterPatterns.put(MemoryUtil.memUTF8("*.jpg"));
            filterPatterns.put(MemoryUtil.memUTF8("*.jpeg"));
            filterPatterns.put(MemoryUtil.memUTF8("*.gif"));
            filterPatterns.put(MemoryUtil.memUTF8("*.bmp"));
            filterPatterns.flip();

            Minecraft.getInstance().getSoundManager().pause();
            String str = TinyFileDialogs.tinyfd_openFileDialog(titleOpenFileDialog.getString(), (CharSequence)null, filterPatterns, "Image Files", false);
            if (str != null) {
                try (InputStream data = DLUtils.scaleImage(new FileInputStream(str), 32, 32)) {
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
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
            }
            Minecraft.getInstance().getSoundManager().resume();
            return false;
        });
        
        for (int k = 0; k < count; k++) {
            final int j = k;
            DLSprite sprite = new DLSprite(resources[j], 16, 16, 0, 0, 32, 32);
            DLToggleButton textureBtn = innerPanel.addComponent(new DLToggleButton(0, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_WIDTH));
            textureBtn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
            textureBtn.radioButtonMode.set(true);
            textureBtn.text.set(TextUtils.EMPTY);
            textureBtn.icon.set(sprite);
            textureBtn.iconAlignment.set(ETextAlignment.CENTER);
            textureBtn.inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);
            textureBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                if (preview != null) {
                    preview.close();
                    preview = null;
                }

                try {
                    preview = new DynamicTexture(NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(resources[j].getTexture().get()).get().open()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return false;
            });
        }
    }

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
        getWindowManager().closeWindow(this);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DefaultGuiTextures.DRAGONLIB_UI.getSprite(DefaultGuiTextures.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());        
        GuiUtils.drawString(graphics, graphics.defaultFont(), WIDTH / 2, 6, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.CENTER, false);

        if (preview != null) {
            GuiUtils.drawTexture(preview.getId(), graphics, 8, 130, 32, 32, 0, 0, 32, 32, TextureFillMode.STRETCH, 32, 32);
        }
    }
}

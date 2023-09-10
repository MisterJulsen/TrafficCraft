package de.mrjulsen.trafficcraft.screen;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.properties.TrafficSignShape;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer;
import de.mrjulsen.trafficcraft.screen.widgets.ControlCollection;
import de.mrjulsen.trafficcraft.screen.widgets.GuiAreaDefinition;
import de.mrjulsen.trafficcraft.screen.widgets.HScrollBar;
import de.mrjulsen.trafficcraft.screen.widgets.ICustomAreaControl;
import de.mrjulsen.trafficcraft.screen.widgets.IconButton;
import de.mrjulsen.trafficcraft.screen.widgets.IconButton.ButtonType;
import de.mrjulsen.trafficcraft.util.Utils;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer.AreaStyle;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer.ColorStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SignPickerScreen extends Screen {

    public static final Component title = new TextComponent("colorpicker");

    private static final int WIDTH = 187;
    private static final int HEIGHT = 171;
    private static final int MAX_ENTRIES_IN_ROW = 9;
    private static final int MAX_ROWS = 6;
    private static final int ICON_BUTTON_WIDTH = 18;
    private static final int ICON_BUTTON_HEIGHT = 18;
      
    private int guiLeft;
    private int guiTop;
    private int selectedId;

    private int scroll = 0;

    private final Screen lastScreen;
    private final TrafficSignShape shape;

    private final ControlCollection groupPatterns = new ControlCollection();
    private HScrollBar scrollbar;

    final ResourceLocation[] resources;
    final int count;

    
    private static final ResourceLocation GUI = new ResourceLocation(ModMain.MOD_ID, "textures/gui/ui.png");

    private TranslatableComponent textTitle = new TranslatableComponent("gui.trafficcraft.colorpicker.title");
    private TranslatableComponent textHSV = new TranslatableComponent("gui.trafficcraft.colorpicker.hsv");
    private TranslatableComponent textRGB = new TranslatableComponent("gui.trafficcraft.colorpicker.rgb");
    private TranslatableComponent textInteger = new TranslatableComponent("gui.trafficcraft.colorpicker.integer");

    public SignPickerScreen(Screen lastScreen, TrafficSignShape shape) {
        super(title);
        this.lastScreen = lastScreen;
        this.shape = shape;
        this.resources = Minecraft.getInstance().getResourceManager().listResources(new ResourceLocation(ModMain.MOD_ID, "textures/block/sign/" + shape.getShape()).getPath(), (x) -> { return x.endsWith(".png"); }).toArray(ResourceLocation[]::new);
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
    public void init() {
        super.init();
        guiLeft = this.width / 2 - WIDTH / 2;
        guiTop = this.height / 2 - (HEIGHT + 24) / 2; 
        
        this.addRenderableWidget(new Button(guiLeft + WIDTH / 2 - 67 + 20, guiTop + HEIGHT - 28, 65, 20, CommonComponents.GUI_DONE, (p) -> {
            this.onDone();
        }));

        this.addRenderableWidget(new Button(guiLeft + WIDTH / 2 + 2 + 20, guiTop + HEIGHT - 28, 65, 20, CommonComponents.GUI_CANCEL, (p) -> {
            this.onClose();
        }));

        
        
        for (int i = 0; i < count; i++) {
            final int j = i;
            IconButton btn = new IconButton(ButtonType.RADIO_BUTTON, ColorStyle.BROWN, groupPatterns, guiLeft + 9, guiTop + 36 + j * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, title, (button) -> {
                selectedId = j;
            }, (button, poseStack, mouseX, mouseY) -> {
                
            }) {
                @Override
                protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                    super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                    RenderSystem.setShaderTexture(0, resources[j]);
                    blit(pPoseStack, x + 1, y + 1, ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2, 0, 0, 32, 32, 32, 32);
                }
            };
            this.addRenderableWidget(btn);
        }

        this.scrollbar = this.addRenderableOnly(new HScrollBar(guiLeft + 171, guiTop + 16, 8, ICON_BUTTON_HEIGHT * MAX_ROWS + 2, new GuiAreaDefinition(guiLeft + 7, guiTop + 16, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_HEIGHT * MAX_ROWS + 2)).setOnValueChangedEvent(v -> {
            this.scroll = v.getScrollValue();
            fillButtons(groupPatterns.components.toArray(IconButton[]::new), this.scroll, guiLeft + 8, guiTop + 17, scrollbar);
        }));

        fillButtons(groupPatterns.components.toArray(IconButton[]::new), this.scroll, guiLeft + 8, guiTop + 17, scrollbar);
    }

    private void onDone() {
        this.onClose();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack, 0);
        AreaRenderer.renderWindow(stack, guiLeft, guiTop, WIDTH, HEIGHT);
        AreaRenderer.renderArea(stack, guiLeft + 7, guiTop + 16, ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW + 2, ICON_BUTTON_HEIGHT * MAX_ROWS + 2, ColorStyle.BROWN, AreaStyle.SUNKEN);

        super.render(stack, mouseX, mouseY, partialTicks);
        
        RenderSystem.setShaderTexture(0, resources[selectedId]);
        blit(stack, guiLeft + 8, guiTop + 130, 32, 32, 0, 0, 32, 32, 32, 32);
    }

    private void fillButtons(IconButton[] buttons, int scrollRow, int defX, int defY, HScrollBar scrollbar) {
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
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.shouldCloseOnEsc() && pKeyCode == 256 || this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
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
}

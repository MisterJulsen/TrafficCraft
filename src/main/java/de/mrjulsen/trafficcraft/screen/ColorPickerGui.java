package de.mrjulsen.trafficcraft.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.screen.widgets.ResizableButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ColorPickerGui extends Screen {

    public static final Component title = new TextComponent("colorpicker");

    private static final int WIDTH = 250;
    private static final int HEIGHT = 185;
    private static final int SELECTION_W = 9;
    private static final int SELECTION_H = 22;
    private static final int SELECTION_Y = 234;

    private static final int AREA_X = 9;
    private static final int COLOR_PICKER_WIDTH = 180;
      
    private int guiLeft;
    private int guiTop;
    private boolean scrollingH = false, scrollingS = false, scrollingV = false;

    private BlockPos blockPos;
    private Level level;
    private Player player;

    // color
    private double h = 0, s = 0, v = 0;

    private TranslatableComponent textTitle = new TranslatableComponent("gui.trafficcraft.colorpicker.title");
    private TranslatableComponent textHSB = new TranslatableComponent("gui.trafficcraft.colorpicker.hsv");
    private TranslatableComponent textRGB = new TranslatableComponent("gui.trafficcraft.colorpicker.rgb");

    private static final ResourceLocation gui = new ResourceLocation(ModMain.MOD_ID, "textures/gui/color_picker.png");

    public ColorPickerGui(BlockPos pos, Level level, Player player) {
        super(title);

        this.blockPos = pos;
        this.level = level;
        this.player = player;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void init() {
        super.init();
        guiLeft = this.width / 2 - WIDTH / 2;
        guiTop = this.height / 2 - (HEIGHT + 24) / 2;

        this.addRenderableWidget(new ResizableButton(guiLeft + 8, guiTop + 18, 48, 16, textHSB, (p) -> {
            
        }));

        this.addRenderableWidget(new ResizableButton(guiLeft + 8 + 52, guiTop + 18, 48, 16, textRGB, (p) -> {
            
        }));

        this.addRenderableWidget(new Button(this.width / 2 - 2 - 115, guiTop + HEIGHT - 28, 115, 20, CommonComponents.GUI_DONE, (p) -> {
            this.onDone();
        }));

        this.addRenderableWidget(new Button(this.width / 2 + 3, guiTop + HEIGHT - 28, 115, 20, CommonComponents.GUI_CANCEL, (p) -> {
            this.onClose();
        }));
    }

    private void onDone() {

    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {

        renderBackground(stack, 0);
        RenderSystem.setShaderTexture(0, gui);

        blit(stack, guiLeft, guiTop, 0, 0, WIDTH, HEIGHT);

        for (int i = 0; i < COLOR_PICKER_WIDTH; i++) {
            ColorObject ch = getH(i, COLOR_PICKER_WIDTH);
            ColorObject cs = getS(h, i, COLOR_PICKER_WIDTH);
            ColorObject cv = getV(h, i, COLOR_PICKER_WIDTH);
            fill(stack, guiLeft + 9 + i, guiTop + 41, guiLeft + 9 + i + 1, guiTop + 57, ch.toInt());
            fill(stack, guiLeft + 9 + i, guiTop + 67, guiLeft + 9 + i + 1, guiTop + 83, cs.toInt());
            fill(stack, guiLeft + 9 + i, guiTop + 93, guiLeft + 9 + i + 1, guiTop + 109, cv.toInt());

        }
        
        // Preview
        fill(stack, guiLeft + 198, guiTop + 10, guiLeft + 198 + 43, guiTop + 10 + 24, ColorObject.fromHSB(h, s, v).toInt());

        String title = textTitle.getString();
        this.font.draw(stack, title, guiLeft + WIDTH / 2 - font.width(title) / 2, guiTop + 6, 4210752);

        // Draw selections
        RenderSystem.setShaderTexture(0, gui);
        blit(stack, guiLeft + 5 + (int)(h * COLOR_PICKER_WIDTH), guiTop + 38, inSliderH(mouseX, mouseY) ? SELECTION_W : 0, SELECTION_Y, SELECTION_W, SELECTION_H);
        blit(stack, guiLeft + 5 + (int)(s * COLOR_PICKER_WIDTH), guiTop + 64, inSliderS(mouseX, mouseY) ? SELECTION_W : 0, SELECTION_Y, SELECTION_W, SELECTION_H);
        blit(stack, guiLeft + 5 + (int)(v * COLOR_PICKER_WIDTH), guiTop + 90, inSliderV(mouseX, mouseY) ? SELECTION_W : 0, SELECTION_Y, SELECTION_W, SELECTION_H);

        this.font.draw(stack, new TextComponent(String.valueOf(h)), guiLeft, guiTop, 4210752);

        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        
        if (button == 0 && inSliderH(mouseX, mouseY)){            
            scrollingH = true;
            this.h = Mth.clamp(setMouseValue(mouseX), 0, 1);
        } else if (button == 0 && inSliderS(mouseX, mouseY)){            
            scrollingS = true;
            this.s = Mth.clamp(setMouseValue(mouseX), 0, 1);
        } else if (button == 0 && inSliderV(mouseX, mouseY)){            
            scrollingV = true;
            this.v = Mth.clamp(setMouseValue(mouseX), 0, 1);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (this.shouldCloseOnEsc() && p_keyPressed_1_ == 256 || this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_))) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {

        if (pButton == 0) {
            this.scrollingH = false;
            this.scrollingS = false;
            this.scrollingV = false;
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {

        double fh = 0;
        if (this.scrollingH || this.scrollingS || this.scrollingV) {
            fh = setMouseValue(pMouseX);
        }

        if (this.scrollingH) {
            this.h = Mth.clamp(fh, 0d, 1d);
            return true;
        } else if (this.scrollingS) {
            this.s = Mth.clamp(fh, 0d, 1d);
            return true;
        } else if (this.scrollingV) {
            this.v = Mth.clamp(fh, 0d, 1d);
            return true;
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    private double setMouseValue(double mouseX) {
        return (mouseX - (double)(this.guiLeft + 9)) / (double)(COLOR_PICKER_WIDTH - 1);
    }

    public static ColorObject getH(int i, int w) {
        float hue = (float) i / w;
        return ColorObject.fromHSB(hue, 1, 1);
    }

    public static ColorObject getS(double h, int i, int w) {
        float hue = (float) i / w;
        return ColorObject.fromHSB(h, hue, 1);
    }

    public static ColorObject getV(double h, int i, int w) {
        float hue = (float) i / w;
        return ColorObject.fromHSB(h, 1, hue);
    }

    protected boolean inSliderH(double mouseX, double mouseY) {
        int x = 9;
        int y = 42;
        int w = 180;
        int h = 16;

        int x1 = guiLeft + x;
        int y1 = guiTop + y;
        int x2 = x1 + w;
        int y2 = y1 + h;

        return mouseX >= (double)x1 && mouseY >= (double)y1 && mouseX < (double)x2 && mouseY < (double)y2;
    }

    protected boolean inSliderS(double mouseX, double mouseY) {
        int x = 9;
        int y = 68;
        int w = 180;
        int h = 16;

        int x1 = guiLeft + x;
        int y1 = guiTop + y;
        int x2 = x1 + w;
        int y2 = y1 + h;

        return mouseX >= (double)x1 && mouseY >= (double)y1 && mouseX < (double)x2 && mouseY < (double)y2;
    }

    protected boolean inSliderV(double mouseX, double mouseY) {
        int x = 9;
        int y = 94;
        int w = 180;
        int h = 16;

        int x1 = guiLeft + x;
        int y1 = guiTop + y;
        int x2 = x1 + w;
        int y2 = y1 + h;

        return mouseX >= (double)x1 && mouseY >= (double)y1 && mouseX < (double)x2 && mouseY < (double)y2;
    }
}

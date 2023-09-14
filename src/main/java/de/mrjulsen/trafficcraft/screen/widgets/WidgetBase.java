package de.mrjulsen.trafficcraft.screen.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class WidgetBase extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {

    public static final ResourceLocation UI = new ResourceLocation("textures/gui/ui.png");

    protected int width;
    protected int height;
    public int x;
    public int y;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0F;
    private boolean focused;
    private Component message;

    public WidgetBase(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        this.x = pX;
        this.y = pY;
        this.width = pWidth;
        this.height = pHeight;
        this.message = pMessage;
    }

    public int getHeight() {
        return this.height;
    }

    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.visible) {            
            this.renderWidget(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    protected MutableComponent createNarrationMessage() {
        return wrapDefaultNarrationMessage(this.getMessage());
    }

    public static MutableComponent wrapDefaultNarrationMessage(Component pMessage) {
        return new TranslatableComponent("gui.narrate.button", pMessage);
    }

    public abstract void renderWidget(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick);

    protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) { }

    public boolean onClick(double pMouseX, double pMouseY) {
        return false;
    }

    public void onRelease(double pMouseX, double pMouseY) { }

    protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) { }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(pButton)) {
                boolean flag = this.clicked(pMouseX, pMouseY);
                if (flag) {
                    if (this.onClick(pMouseX, pMouseY)) {                        
                        this.playDownSound(Minecraft.getInstance().getSoundManager());
                    }
                    return true;
                }
            }   
            return false;
        } else {
            return false;
        }
    }

    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (this.isValidClickButton(pButton)) {
            this.onRelease(pMouseX, pMouseY);
            return true;
        } else {
            return false;
        }
    }

    protected boolean isValidClickButton(int pButton) {
        return pButton == 0;
    }

    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.isValidClickButton(pButton)) {
            this.onDrag(pMouseX, pMouseY, pDragX, pDragY);
            return true;
        } else {
            return false;
        }
    }

    protected boolean clicked(double pMouseX, double pMouseY) {
        return this.active && this.visible && pMouseX >= (double) this.x && pMouseY >= (double) this.y && pMouseX < (double) (this.x + this.width) && pMouseY < (double) (this.y + this.height);
    }

    public boolean isHoveredOrFocused() {
        return this.isHovered || this.focused;
    }

    public boolean changeFocus(boolean pFocus) {
        if (this.active && this.visible) {
            this.focused = !this.focused;
            this.onFocusedChanged(this.focused);
            return this.focused;
        } else {
            return false;
        }
    }

    protected void onFocusedChanged(boolean pFocused) { }

    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return this.active && this.visible && pMouseX >= (double) this.x && pMouseY >= (double) this.y && pMouseX < (double) (this.x + this.width) && pMouseY < (double) (this.y + this.height);
    }

    public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) { }

    public void playDownSound(SoundManager pHandler) {
        pHandler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int pWidth) {
        this.width = pWidth;
    }

    public void setHeight(int value) {
        this.height = value;
    }

    public void setMessage(Component pMessage) {
        this.message = pMessage;
    }

    public Component getMessage() {
        return this.message;
    }

    public void setAlpha(float pAlpha) {
        this.alpha = pAlpha;
    }

    public boolean isFocused() {
        return this.focused;
    }

    public boolean isActive() {
        return this.visible && this.active;
    }

    protected void setFocused(boolean pFocused) {
        this.focused = pFocused;
    }

    public static final int UNSET_FG_COLOR = -1;
    protected int packedFGColor = UNSET_FG_COLOR;

    public int getFGColor() {
        if (packedFGColor != UNSET_FG_COLOR)
            return packedFGColor;
        return this.active ? 16777215 : 10526880; // White : Light Grey
    }

    public void setFGColor(int color) {
        this.packedFGColor = color;
    }

    public void clearFGColor() {
        this.packedFGColor = UNSET_FG_COLOR;
    }

    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.focused) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        } else {
            return this.isHovered ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
        }
    }

    protected void defaultButtonNarrationText(NarrationElementOutput p_168803_) {
        p_168803_.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                p_168803_.add(NarratedElementType.USAGE, new TranslatableComponent("narration.button.usage.focused"));
            } else {
                p_168803_.add(NarratedElementType.USAGE, new TranslatableComponent("narration.button.usage.hovered"));
            }
        }

    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        this.defaultButtonNarrationText(pNarrationElementOutput);
    }
}
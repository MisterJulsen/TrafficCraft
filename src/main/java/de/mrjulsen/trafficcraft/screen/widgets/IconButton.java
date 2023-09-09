package de.mrjulsen.trafficcraft.screen.widgets;

import org.jline.utils.Colors;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer.AreaStyle;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer.ColorStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IconButton extends Button {

    private boolean selected = false;
    private final ButtonType type;
    private final ControlCollection collection;

    public IconButton(ButtonType type, ControlCollection collection, int pX, int pY, int w, int h, Component pMessage, OnPress pOnPress) {
        super(pX, pY, w, h, pMessage, pOnPress);
        this.type = type;

        if (collection != null) {
            collection.components.add(this);
        }
        this.collection = collection;
    }

    public IconButton(ButtonType type, ControlCollection collection, int pX, int pY, int w, int h, Component pMessage, OnPress pOnPress, OnTooltip pOnTooltip) {
        super(pX, pY, w, h, pMessage, pOnPress, pOnTooltip);
        this.type = type;

        if (collection != null) {
            collection.components.add(this);
        }
        this.collection = collection;
    }

    public boolean isSelected() {
        return selected;
    }

    public void deselect() {
        this.selected = false;
    }

    public void select() {
        this.selected = true;
    }

    public void toggleSelection() {
        this.selected = !this.selected;
    }

    @Override
    public void onPress() {

        switch (type) {
            case RADIO_BUTTON:
                if (selected) {
                    return;
                }
                selected = true;

                if (collection != null) {
                    collection.performForEach((x) -> { return x instanceof IconButton && x != this; }, (x) -> ((IconButton)x).deselect());
                }
                break;
            case TOGGLE_BUTTON:
                this.toggleSelection();
                break;
            default:
                break;
        }       
        
        super.onPress();
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        AreaRenderer.renderArea(pPoseStack, x, y, width, height, ColorStyle.BROWN, active ? (selected ? AreaStyle.SUNKEN : (isHovered ? AreaStyle.SELECTED : AreaStyle.BUTTON)) : AreaStyle.RAISED);
        this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
    }

    public enum ButtonType {
        DEFAULT,
        RADIO_BUTTON,
        TOGGLE_BUTTON;
    }
    
}

package de.mrjulsen.trafficcraft.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer.BrownAreaStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IconButton extends Button {

    public static final int WIDTH = 18;
    public static final int HEIGHT = 18;

    private boolean selected = false;
    private final ButtonType type;
    private final ControlCollection collection;

    public IconButton(ButtonType type, ControlCollection collection, int pX, int pY, Component pMessage, OnPress pOnPress) {
        super(pX, pY, WIDTH, HEIGHT, pMessage, pOnPress);
        this.type = type;

        if (collection != null) {
            collection.components.add(this);
        }
        this.collection = collection;
    }

    public IconButton(ButtonType type, ControlCollection collection, int pX, int pY, Component pMessage, OnPress pOnPress, OnTooltip pOnTooltip) {
        super(pX, pY, WIDTH, HEIGHT, pMessage, pOnPress, pOnTooltip);
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
        AreaRenderer.renderBrownArea(pPoseStack, x, y, WIDTH, HEIGHT, selected ? BrownAreaStyle.SUNKEN : (isHovered ? BrownAreaStyle.SELECTED : BrownAreaStyle.BUTTON));
        this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
    }

    public enum ButtonType {
        DEFAULT,
        RADIO_BUTTON,
        TOGGLE_BUTTON;
    }
    
}

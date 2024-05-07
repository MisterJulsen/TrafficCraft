package de.mrjulsen.legacydragonlib.client.gui.widgets;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.legacydragonlib.DragonLibConstants;
import de.mrjulsen.legacydragonlib.client.gui.DynamicGuiRenderer;
import de.mrjulsen.legacydragonlib.client.gui.WidgetsCollection;
import de.mrjulsen.legacydragonlib.client.gui.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.legacydragonlib.client.gui.DynamicGuiRenderer.ButtonState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractImageButton<T extends AbstractImageButton<T>> extends Button {

    private boolean selected = false;
    private final WidgetsCollection collection;
    private ButtonType type;
    private AreaStyle style;
    private Alignment alignment = Alignment.CENTER;
    private int color = DragonLibConstants.DEFAULT_UI_FONT_COLOR;

    public AbstractImageButton(ButtonType type, AreaStyle style, int pX, int pY, int w, int h, Component pMessage, Consumer<Button> onClick) {
        this(type, style, null, pX, pY, w, h, pMessage, onClick);
    }

    public AbstractImageButton(ButtonType type, AreaStyle style, WidgetsCollection collection, int pX, int pY, int w, int h, Component pMessage, Consumer<Button> onClick) {
        super(pX, pY, w, h, pMessage, (btn) -> onClick.accept(btn), NO_TOOLTIP);
        withButtonType(type);
        withStyle(style);

        if (collection != null) {
            collection.components.add(this);
        }
        this.collection = collection;
    }

    public ButtonType getButtonType() {
        return type;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public AreaStyle getStyle() {
        return style;
    }

    public int getFontColor() {
        return color;
    }

    @SuppressWarnings("unchecked")
    public T withAlignment(Alignment alignment) {
        this.alignment = alignment;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T withStyle(AreaStyle style) {
        this.style = style;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T withButtonType(ButtonType type) {
        this.type = type;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T withFontColor(int color) {
        this.color = color;
        return (T)this;
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
                    collection.performForEach((x) -> { return x instanceof AbstractImageButton && x != this; }, (x) -> ((AbstractImageButton<?>)x).deselect());
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
        DynamicGuiRenderer.renderArea(pPoseStack, x, y, width, height, this.style, active ? (selected ? ButtonState.SUNKEN : (isHovered ? ButtonState.SELECTED : ButtonState.BUTTON)) : ButtonState.RAISED);
        this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
        renderImage(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    public abstract void renderImage(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick);

    public enum ButtonType {
        DEFAULT,
        RADIO_BUTTON,
        TOGGLE_BUTTON;
    }

    public enum Alignment {
        RIGHT,
        CENTER,
        LEFT;
    }
    
}


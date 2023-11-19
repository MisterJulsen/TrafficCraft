package de.mrjulsen.trafficcraft.client.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.client.widgets.AreaRenderer.AreaStyle;
import de.mrjulsen.trafficcraft.client.widgets.AreaRenderer.ColorStyle;
import de.mrjulsen.trafficcraft.client.widgets.IconButton.ButtonType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemButton extends Button {

    public static final int DEFAULT_BUTTON_WIDTH = 18;
    public static final int DEFAULT_BUTTON_HEIGHT = 18;

    private boolean selected = false;
    private final ButtonType type;
    private final ControlCollection collection;
    private final ColorStyle style;
    private final ItemStack item;

    public ItemButton(ButtonType type, ColorStyle color, ControlCollection collection, ItemStack item, int pX, int pY, OnPress pOnPress) {
        this(type, color, collection, item, pX, pY, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, pOnPress, NO_TOOLTIP);
    }

    public ItemButton(ButtonType type, ColorStyle color, ControlCollection collection, ItemStack item, int pX, int pY, OnPress pOnPress, OnTooltip pOnTooltip) {
        this(type, color, collection, item, pX, pY, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, pOnPress, pOnTooltip);
    }

    public ItemButton(ButtonType type, ColorStyle color, ControlCollection collection, ItemStack item, int pX, int pY, int w, int h, OnPress pOnPress) {
        this(type, color, collection, item, pX, pY, w, h, pOnPress, NO_TOOLTIP);
    }

    public ItemButton(ButtonType type, ColorStyle color, ControlCollection collection, ItemStack item, int pX, int pY, int w, int h, OnPress pOnPress, OnTooltip pOnTooltip) {
        super(pX, pY, w, h, item.getHoverName(), pOnPress, pOnTooltip);
        this.type = type;
        this.style = color;
        this.item = item;

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
                    collection.performForEach((x) -> { return x instanceof ItemButton && x != this; }, (x) -> ((ItemButton)x).deselect());
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
        AreaRenderer.renderArea(pPoseStack, x, y, width, height, this.style, active ? (selected ? AreaStyle.SUNKEN : (isHovered ? AreaStyle.SELECTED : AreaStyle.BUTTON)) : AreaStyle.RAISED);
        this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
        minecraft.getItemRenderer().renderAndDecorateItem(item, x + 1, y + 1);
        if (width > DEFAULT_BUTTON_WIDTH) {
            minecraft.font.draw(pPoseStack, getMessage(), x + 19, y + 5, 4210752);
        }
    }

    public ItemStack getItem() {
        return item;
    }
    
}

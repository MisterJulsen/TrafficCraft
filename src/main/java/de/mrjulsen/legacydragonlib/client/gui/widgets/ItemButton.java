package de.mrjulsen.legacydragonlib.client.gui.widgets;

import java.util.function.Consumer;

import de.mrjulsen.legacydragonlib.client.gui.GuiUtils;
import de.mrjulsen.legacydragonlib.client.gui.WidgetsCollection;
import de.mrjulsen.legacydragonlib.client.gui.DynamicGuiRenderer.AreaStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemButton extends AbstractImageButton<ItemButton> {

    public static final int DEFAULT_BUTTON_WIDTH = 18;
    public static final int DEFAULT_BUTTON_HEIGHT = 18;

    private ItemStack item;
    private boolean renderItemTooltip = true;

    public ItemButton(ButtonType type, AreaStyle color, ItemStack item, WidgetsCollection collection, int pX, int pY, int w, int h, Component customText, Consumer<Button> onClick) {
        super(type, color, collection, pX, pY, w, h, customText == null ? item.getHoverName() : customText, onClick);
        withItem(item);

        if (color == AreaStyle.NATIVE) {
            withFontColor(getFGColor());
        }
    }

    public ItemButton(ButtonType type, AreaStyle color, ItemStack item, int pX, int pY, int w, int h, Component customText, Consumer<Button> onClick) {
        this(type, color, item, null, pX, pY, w, h, customText, onClick);
    }

    public ItemButton(ButtonType type, AreaStyle color, ItemStack item, WidgetsCollection collection, int pX, int pY, Component customText, Consumer<Button> onClick) {
        this(type, color, item, collection, pX, pY, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, customText, onClick);
    }

    public ItemButton(ButtonType type, AreaStyle color, ItemStack item, int pX, int pY, Component customText, Consumer<Button> onClick) {
        this(type, color, item, pX, pY, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, customText, onClick);
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemButton withItem(ItemStack stack) {
        this.item = stack;
        return this;
    }

    public ItemButton withDefaultItemTooltip(boolean b) {
        this.renderItemTooltip = b;
        return this;
    }

    @Override
    @SuppressWarnings("resource")
    public void renderImage(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        Font font = Minecraft.getInstance().font;
        int labelWidth = 0;
        switch (getAlignment()) {            
            case LEFT:
                if (this.getMessage() != null) {
                    String txt = font.ellipsize(getMessage(), getWidth() - 4 - 16 - 4).getString();
                    graphics.drawString(font, txt, getX() + 2 + 16 + 4, getY() + height / 2 - font.lineHeight / 2, getFontColor(), false);
                }
                graphics.renderItem(item, getX() + 2, getY() + height / 2 - 8);
                break;
            case RIGHT:
                if (this.getMessage() != null) {
                    String txt = font.ellipsize(getMessage(), getWidth() - 4 - 16 - 4).getString();
                    labelWidth = font.width(txt) + 4;
                    graphics.drawString(font, txt, getX() + width - 2 - labelWidth + 4, getY() + height / 2 - font.lineHeight / 2, getFontColor(), false);
                }
                graphics.renderItem(item, getX() + width - 2 - labelWidth - 16, getY() + height / 2 - 8);
                break;
            case CENTER:
            default:
                if (this.getMessage() != null) {
                    String txt = font.ellipsize(getMessage(), getWidth() - 4 - 16 - 4).getString();
                    labelWidth = font.width(txt) + 4;
                    graphics.drawString(font, txt, getX() + width / 2 + 8 - labelWidth / 2 + 4, getY() + height / 2 - font.lineHeight / 2, getFontColor(), false);
                }
                graphics.renderItem(item, getX() + width / 2 - 8 - labelWidth / 2, getY() + height / 2 - 8);
                break;
        }
        
    }

    public void renderTooltip(Screen parent, GuiGraphics graphics, int pMouseX, int pMouseY) {
        if (isMouseOver(pMouseX, pMouseY) && renderItemTooltip) {
            GuiUtils.renderTooltip(parent, this, Screen.getTooltipFromItem(Minecraft.getInstance(), getItem()), -1, graphics, pMouseX, pMouseY);
        }
    }

    public static void renderAllItemButtonTooltips(Screen parent, GuiGraphics graphics, int pMouseX, int pMouseY) {
        parent.renderables.stream().filter(x -> x instanceof ItemButton b && b.isMouseOver(pMouseX, pMouseY) && b.renderItemTooltip).forEach(x -> ((ItemButton)x).renderTooltip(parent, graphics, pMouseX, pMouseY));
    }
}


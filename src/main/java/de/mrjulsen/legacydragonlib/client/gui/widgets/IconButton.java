package de.mrjulsen.legacydragonlib.client.gui.widgets;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.legacydragonlib.client.gui.Sprite;
import de.mrjulsen.legacydragonlib.client.gui.WidgetsCollection;
import de.mrjulsen.legacydragonlib.client.gui.DynamicGuiRenderer.AreaStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IconButton extends AbstractImageButton<IconButton> {

    public static final int DEFAULT_BUTTON_WIDTH = 18;
    public static final int DEFAULT_BUTTON_HEIGHT = 18;

    private Sprite sprite;

    public IconButton(ButtonType type, AreaStyle color, Sprite sprite, WidgetsCollection collection, int pX, int pY, int w, int h, Component pMessage, Consumer<Button> onClick) {
        super(type, color, collection, pX, pY, w, h, pMessage, onClick);
        this.sprite = sprite;        

        if (color == AreaStyle.NATIVE) {
            withFontColor(getFGColor());
        }
    }

    public IconButton(ButtonType type, AreaStyle color, Sprite sprite, int pX, int pY, int w, int h, Component pMessage, Consumer<Button> onClick) {
        this(type, color, sprite, null, pX, pY, w, h, pMessage, onClick);
    }

    public IconButton(ButtonType type, AreaStyle color, Sprite sprite, WidgetsCollection collection, int pX, int pY, Component pMessage, Consumer<Button> onClick) {
        this(type, color, sprite, collection, pX, pY, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, pMessage, onClick);
    }

    public IconButton(ButtonType type, AreaStyle color, Sprite sprite, int pX, int pY, Component pMessage, Consumer<Button> onClick) {
        this(type, color, sprite, pX, pY, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, pMessage, onClick);
    }

    @Override
    @SuppressWarnings("resource")
    public void renderImage(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        Font font = Minecraft.getInstance().font;
        int labelWidth = 0;
        switch (getAlignment()) {            
            case LEFT:
                if (this.getMessage() != null) {
                    font.draw(pPoseStack, getMessage(), x + 2 + 4 + sprite.getWidth(), y + height / 2 - font.lineHeight / 2, getFontColor());
                }
                sprite.render(pPoseStack, x + 2, y + height / 2 - sprite.getHeight() / 2);
                break;
            case RIGHT:
                if (this.getMessage() != null) {
                    labelWidth = font.width(this.getMessage()) + 4;
                    font.draw(pPoseStack, getMessage(), x + width - 2 + 4 - labelWidth, y + height / 2 - font.lineHeight / 2, getFontColor());
                }
                sprite.render(pPoseStack, x + width - 2 - labelWidth - sprite.getWidth(), y + height / 2 - sprite.getHeight() / 2);
                break;
            case CENTER:
            default:
                if (this.getMessage() != null) {
                    labelWidth = font.width(this.getMessage()) + 4;
                    font.draw(pPoseStack, getMessage(), x + width / 2 + sprite.getWidth() / 2 - labelWidth / 2 + 4, y + height / 2 - font.lineHeight / 2, getFontColor());
                }
                sprite.render(pPoseStack, x + width / 2 - sprite.getWidth() / 2 - labelWidth / 2, y + height / 2 - sprite.getHeight() / 2);
                break;
        }
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public Sprite getSprite() {
        return sprite;
    }
}
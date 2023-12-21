package de.mrjulsen.trafficcraft.client;

import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.AreaStyle;

import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.client.gui.Sprite;
import de.mrjulsen.mcdragonlib.client.gui.WidgetsCollection;
import de.mrjulsen.mcdragonlib.client.gui.widgets.AbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IconButton;
import de.mrjulsen.trafficcraft.block.data.IIconEnum;
import net.minecraft.client.gui.components.Button;

public class ModGuiUtils {

    public static IconButton createCopyButton(int x, int y, WidgetsCollection collection, AreaStyle style, Consumer<Button> onClick) {
        return new IconButton(
                ButtonType.DEFAULT,
                AreaStyle.GRAY,
                new Sprite(IIconEnum.ICON_TEXTURE_LOCATION, IIconEnum.TEXTURE_SIZE, IIconEnum.TEXTURE_SIZE,
                        IIconEnum.DEFAULT_SPRITE_SIZE * 0, IIconEnum.DEFAULT_SPRITE_SIZE * 3,
                        IIconEnum.DEFAULT_SPRITE_SIZE, IIconEnum.DEFAULT_SPRITE_SIZE),
                collection,
                x,
                y,
                IconButton.DEFAULT_BUTTON_HEIGHT,
                IconButton.DEFAULT_BUTTON_HEIGHT,
                null,
                onClick
        );
    }

    public static IconButton createPasteButton(int x, int y, WidgetsCollection collection, AreaStyle style, Consumer<Button> onClick) {
        return new IconButton(
                ButtonType.DEFAULT,
                AreaStyle.GRAY,
                new Sprite(IIconEnum.ICON_TEXTURE_LOCATION, IIconEnum.TEXTURE_SIZE, IIconEnum.TEXTURE_SIZE,
                        IIconEnum.DEFAULT_SPRITE_SIZE * 1, IIconEnum.DEFAULT_SPRITE_SIZE * 3,
                        IIconEnum.DEFAULT_SPRITE_SIZE, IIconEnum.DEFAULT_SPRITE_SIZE),
                collection,
                x,
                y,
                IconButton.DEFAULT_BUTTON_HEIGHT,
                IconButton.DEFAULT_BUTTON_HEIGHT,
                null,
                onClick
            );
    }

    public static boolean editBoxPositiveNumberFilter(String input) {
        if (input.isEmpty()) {
            return true;
        } else {
            try {
                int i = Integer.parseInt(input);
                return i > 0;
            } catch (NumberFormatException var3) {
                return false;
            }
        }
    }

    public static boolean editBoxNonNegativeNumberFilter(String input) {
        if (input.isEmpty()) {
            return true;
        } else {
            try {
                int i = Integer.parseInt(input);
                return i >= 0;
            } catch (NumberFormatException var3) {
                return false;
            }
        }
    }
}

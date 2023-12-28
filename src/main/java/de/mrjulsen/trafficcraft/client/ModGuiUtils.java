package de.mrjulsen.trafficcraft.client;

import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.AreaStyle;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.client.gui.Sprite;
import de.mrjulsen.mcdragonlib.client.gui.Tooltip;
import de.mrjulsen.mcdragonlib.client.gui.WidgetsCollection;
import de.mrjulsen.mcdragonlib.client.gui.widgets.AbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IconButton;
import de.mrjulsen.trafficcraft.block.data.IIconEnum;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class ModGuiUtils {

    public static IconButton createCopyButton(int x, int y, WidgetsCollection collection, AreaStyle style, Consumer<Button> onClick) {
        return createCopyButton(x, y, IconButton.DEFAULT_BUTTON_WIDTH, IconButton.DEFAULT_BUTTON_HEIGHT, collection, style, onClick);
    }

    public static IconButton createPasteButton(int x, int y, WidgetsCollection collection, AreaStyle style, Consumer<Button> onClick) {
        return createPasteButton(x, y, IconButton.DEFAULT_BUTTON_WIDTH, IconButton.DEFAULT_BUTTON_HEIGHT, collection, style, onClick);
    }

    public static IconButton createCopyButton(int x, int y, int width, int height, WidgetsCollection collection, AreaStyle style, Consumer<Button> onClick) {
        return new IconButton(
            ButtonType.DEFAULT,
            style,
            new Sprite(IIconEnum.ICON_TEXTURE_LOCATION, IIconEnum.TEXTURE_SIZE, IIconEnum.TEXTURE_SIZE,
                    IIconEnum.DEFAULT_SPRITE_SIZE * 0, IIconEnum.DEFAULT_SPRITE_SIZE * 3,
                    IIconEnum.DEFAULT_SPRITE_SIZE, IIconEnum.DEFAULT_SPRITE_SIZE),
            collection,
            x,
            y,
            width,
            height,
            null,
            onClick
        );
    }

    public static IconButton createPasteButton(int x, int y, int width, int height, WidgetsCollection collection, AreaStyle style, Consumer<Button> onClick) {
        return new IconButton(
            ButtonType.DEFAULT,
            style,
            new Sprite(IIconEnum.ICON_TEXTURE_LOCATION, IIconEnum.TEXTURE_SIZE, IIconEnum.TEXTURE_SIZE,
                    IIconEnum.DEFAULT_SPRITE_SIZE * 1, IIconEnum.DEFAULT_SPRITE_SIZE * 3,
                    IIconEnum.DEFAULT_SPRITE_SIZE, IIconEnum.DEFAULT_SPRITE_SIZE),
            collection,
            x,
            y,
            width,
            height,
            null,
            onClick
        );
    }

    public static HelpButtonComponents createHelpButton(Screen parent, int x, int y, int width, int height, WidgetsCollection collection, AreaStyle style, String url, Component title, Collection<Component> description) {
        IconButton btn = new IconButton(
            ButtonType.DEFAULT,
            style,
            new Sprite(IIconEnum.ICON_TEXTURE_LOCATION, IIconEnum.TEXTURE_SIZE, IIconEnum.TEXTURE_SIZE,
                        IIconEnum.DEFAULT_SPRITE_SIZE * 2, IIconEnum.DEFAULT_SPRITE_SIZE * 3,
                        IIconEnum.DEFAULT_SPRITE_SIZE, IIconEnum.DEFAULT_SPRITE_SIZE),
            collection,
            x,
            y,
            width,
            height,
            null,
            (b) -> {
                Minecraft.getInstance().setScreen(new ConfirmLinkScreen((bool) -> {                    
                    if (bool) {
                        Util.getPlatform().openUri(URI.create(url));
                    }
                    Minecraft.getInstance().setScreen(parent);
                }, url, true));
            }
        );

        List<FormattedText> list = new ArrayList<>(List.of(title));
        list.addAll(description);

        Tooltip tooltip = Tooltip
            .of(list)
            .withMaxWidth(parent.width / 4)
            .assignedTo(btn)
        ;

        return new HelpButtonComponents(btn, tooltip);
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

    public static record HelpButtonComponents(IconButton helpButton, Tooltip tooltip) {}
}
